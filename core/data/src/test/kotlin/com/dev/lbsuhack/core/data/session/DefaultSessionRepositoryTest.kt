package com.dev.lbsuhack.core.data.session

import com.dev.lbsuhack.core.data.FakeKeyValueStorage
import com.dev.lbsuhack.core.data.FakeTokenStorage
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionClearHook
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionRepositoryTest {

    private fun storageWithUser(
        id: String? = "user-1",
        name: String? = "Ann",
        role: String? = "TEACHER",
    ): FakeKeyValueStorage = FakeKeyValueStorage(
        buildMap {
            if (id != null) put("id", id)
            if (name != null) put("name", name)
            if (role != null) put("role", role)
            put("surname", "Lee")
            put("schools_by_id", "schools")
            put("school_name", "School 1")
            put("pupil_class", "9A")
        },
    )

    private fun kotlinx.coroutines.test.TestScope.repository(
        storage: FakeKeyValueStorage,
        tokenStorage: FakeTokenStorage = FakeTokenStorage(accessToken = "access"),
        hooks: List<SessionClearHook> = emptyList(),
    ): DefaultSessionRepository = DefaultSessionRepository(
        storage = storage,
        tokenStorage = tokenStorage,
        ioDispatcher = StandardTestDispatcher(testScheduler),
        clearHooks = { hooks },
    )

    @Test
    fun constructorDropsLegacyFathersName() = runTest {
        val storage = FakeKeyValueStorage(mapOf("fathers_name" to "Ivan"))

        repository(storage)

        assertNull(storage.getString("fathers_name"))
    }

    @Test
    fun restoreAuthenticatesUserWhenAllFieldsAndTokenPresent() = runTest {
        val storage = storageWithUser()
        val repository = repository(storage)

        repository.restore()

        val user = (repository.state.value as SessionState.Authenticated).user
        assertEquals("user-1", user.id)
        assertEquals("Ann", user.name)
        assertEquals("Lee", user.surname)
        assertEquals("schools", user.schoolsById)
        assertEquals("School 1", user.schoolName)
        assertEquals(UserRole.TEACHER, user.role)
        assertEquals("9A", user.pupilClass)
    }

    @Test
    fun restoreUnauthenticatesWhenIdIsBlank() = runTest {
        val repository = repository(storageWithUser(id = " "))

        repository.restore()

        assertEquals(SessionState.Unauthenticated, repository.state.value)
    }

    @Test
    fun restoreUnauthenticatesWhenNameMissing() = runTest {
        val repository = repository(storageWithUser(name = null))

        repository.restore()

        assertEquals(SessionState.Unauthenticated, repository.state.value)
    }

    @Test
    fun restoreUnauthenticatesWhenAccessTokenMissing() = runTest {
        val repository = repository(
            storage = storageWithUser(),
            tokenStorage = FakeTokenStorage(accessToken = null),
        )

        repository.restore()

        assertEquals(SessionState.Unauthenticated, repository.state.value)
    }

    @Test
    fun restoreFallsBackToPupilRoleOnUnknownValue() = runTest {
        val repository = repository(storageWithUser(role = "SUPERHERO"))

        repository.restore()

        val user = (repository.state.value as SessionState.Authenticated).user
        assertEquals(UserRole.PUPIL, user.role)
    }

    @Test
    fun savePersistsUserFieldsAndAuthenticates() = runTest {
        val storage = FakeKeyValueStorage()
        val repository = repository(storage)
        val user = SessionUser(
            id = "user-2",
            name = "Bob",
            surname = null,
            schoolsById = "schools-2",
            schoolName = "School 2",
            role = UserRole.PUPIL,
            pupilClass = "5B",
        )

        repository.save(user)

        assertEquals(SessionState.Authenticated(user), repository.state.value)
        assertEquals("user-2", storage.getString("id"))
        assertEquals("Bob", storage.getString("name"))
        assertNull(storage.getString("surname"))
        assertEquals("schools-2", storage.getString("schools_by_id"))
        assertEquals("School 2", storage.getString("school_name"))
        assertEquals("PUPIL", storage.getString("role"))
        assertEquals("5B", storage.getString("pupil_class"))
    }

    @Test
    fun restoreIsIgnoredAfterSave() = runTest {
        val storage = storageWithUser()
        val repository = repository(storage)
        repository.restore()
        val secondUser = SessionUser(id = "user-2", name = "Bob")
        repository.save(secondUser)
        storage.values["id"] = "user-1"

        repository.restore()

        assertEquals(SessionState.Authenticated(secondUser), repository.state.value)
    }

    @Test
    fun restoreIsIgnoredAfterClear() = runTest {
        val storage = storageWithUser()
        val tokenStorage = FakeTokenStorage(accessToken = "access")
        val repository = repository(storage, tokenStorage)
        repository.restore()
        repository.clear()
        storage.values["id"] = "user-1"
        storage.values["name"] = "Ann"
        tokenStorage.updateTokens(accessToken = "access")

        repository.restore()

        assertEquals(SessionState.Unauthenticated, repository.state.value)
    }

    @Test
    fun clearSkipsHooksWhenSessionNotAuthenticated() = runTest {
        var hookCalled = false
        val repository = repository(
            storage = FakeKeyValueStorage(),
            hooks = listOf(SessionClearHook { hookCalled = true }),
        )

        repository.clear()

        assertFalse(hookCalled)
        assertEquals(SessionState.Unauthenticated, repository.state.value)
    }

    @Test
    fun clearRunsHooksWhenAuthenticated() = runTest {
        var hookCalled = false
        val repository = repository(
            storage = storageWithUser(),
            hooks = listOf(SessionClearHook { hookCalled = true }),
        )
        repository.restore()

        repository.clear()

        assertTrue(hookCalled)
        assertEquals(SessionState.Unauthenticated, repository.state.value)
    }

    @Test
    fun clearAbandonsHookExceedingTimeout() = runTest {
        var hookCompleted = false
        val slowHook = SessionClearHook {
            delay(10_000)
            hookCompleted = true
        }
        val repository = repository(
            storage = storageWithUser(),
            hooks = listOf(slowHook),
        )
        repository.restore()

        repository.clear()

        assertFalse(hookCompleted)
        assertEquals(SessionState.Unauthenticated, repository.state.value)
    }

    @Test
    fun clearWipesStorageAndTokens() = runTest {
        val storage = storageWithUser()
        val tokenStorage = FakeTokenStorage(accessToken = "access")
        val repository = repository(storage, tokenStorage)
        repository.restore()

        repository.clear()

        assertTrue(storage.values.isEmpty())
        assertEquals(1, tokenStorage.clearedCount)
    }
}
