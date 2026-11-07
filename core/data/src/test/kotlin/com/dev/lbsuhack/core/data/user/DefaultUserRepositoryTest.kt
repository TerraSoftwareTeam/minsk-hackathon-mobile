package com.dev.lbsuhack.core.data.user

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import com.dev.lbsuhack.core.data.FakeKeyValueStorage
import com.dev.lbsuhack.core.data.FakeSessionRepository
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultUserRepositoryTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private var requestCount = 0

    private fun TestScope.repository(
        storage: FakeKeyValueStorage = FakeKeyValueStorage(),
        sessionState: SessionState = SessionState.Authenticated(
            SessionUser(id = "user-1", name = "Ann", schoolName = "Session School"),
        ),
        sessionRepository: FakeSessionRepository = FakeSessionRepository(sessionState),
        handler: MockRequestHandler = {
            requestCount += 1
            respondJson(pupilResponse())
        },
    ): DefaultUserRepository {
        val client = HttpClient(MockEngine(handler)) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }
        return DefaultUserRepository(
            storage = storage,
            httpClient = client,
            sessionRepository = sessionRepository,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
    }

    private fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(
            content = body,
            status = status,
            headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
        )

    private fun pupilResponse(
        id: String = "user-1",
        name: String = "Fresh",
        role: String = "PUPIL",
        pupilClass: String? = "9A",
    ): String {
        val classPart = if (pupilClass == null) "" else ""","class":"$pupilClass""""
        return """{"id":"$id","schoolsById":"schools","name":"$name","surname":"Lee"$classPart,"role":"$role"}"""
    }

    @Test
    fun oldResponseCannotRestoreProfileAfterLogoutAndLogin() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = repository(handler = {
            gate.await()
            respondJson(pupilResponse(name = "Old profile"))
        })
        val pending = async { repository.getCurrentUser(forceRefresh = true) }
        runCurrent()

        repository.beforeClear()
        assertNull(repository.currentUser.value)
        repository.updateCache(User.Pupil(id = "user-1", name = "New profile", pupilClass = "9A"))
        gate.complete(Unit)

        assertEquals(DomainError.Unauthorized, (pending.await() as DomainResult.Failure).error)
        assertEquals("New profile", repository.currentUser.value?.name)
        assertEquals("New profile", (repository.getCurrentUser() as DomainResult.Success).value.name)
    }

    @Test
    fun oldResponseIsDiscardedAfterSwitchingAccounts() = runTest {
        val session = FakeSessionRepository(SessionState.Authenticated(SessionUser("user-1", "First")))
        val gate = CompletableDeferred<Unit>()
        val repository = repository(sessionRepository = session, handler = {
            gate.await()
            respondJson(pupilResponse())
        })
        val pending = async { repository.getCurrentUser(forceRefresh = true) }
        runCurrent()
        session.save(SessionUser("user-2", "Second"))
        gate.complete(Unit)

        assertEquals(DomainError.Unauthorized, (pending.await() as DomainResult.Failure).error)
        assertNull(repository.currentUser.value)
    }

    @Test
    fun failsWithUnauthorizedWhenSessionNotAuthenticated() = runTest {
        val repository = repository(sessionState = SessionState.Unauthenticated)

        val result = repository.getCurrentUser()

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Unauthorized, (result as DomainResult.Failure).error)
        assertEquals(0, requestCount)
    }

    @Test
    fun returnsCachedUserWithoutNetworkWhenIdsMatch() = runTest {
        val storage = FakeKeyValueStorage(
            mapOf(
                "id" to "user-1",
                "name" to "Cached",
                "role" to "PUPIL",
                "pupil_class" to "8B",
            ),
        )
        val repository = repository(storage)

        val result = repository.getCurrentUser()

        val user = (result as DomainResult.Success).value as User.Pupil
        assertEquals("Cached", user.name)
        assertEquals("8B", user.pupilClass)
        assertEquals(0, requestCount)
    }

    @Test
    fun ignoresCacheBelongingToAnotherUser() = runTest {
        val storage = FakeKeyValueStorage(
            mapOf(
                "id" to "user-2",
                "name" to "Stranger",
                "role" to "PUPIL",
            ),
        )
        val repository = repository(storage)

        val result = repository.getCurrentUser()

        val user = (result as DomainResult.Success).value
        assertEquals("user-1", user.id)
        assertEquals("Fresh", user.name)
        assertEquals(1, requestCount)
    }

    @Test
    fun forceRefreshBypassesMatchingCache() = runTest {
        val storage = FakeKeyValueStorage(
            mapOf(
                "id" to "user-1",
                "name" to "Cached",
                "role" to "PUPIL",
            ),
        )
        val repository = repository(storage)

        val result = repository.getCurrentUser(forceRefresh = true)

        assertEquals("Fresh", (result as DomainResult.Success).value.name)
        assertEquals(1, requestCount)
    }

    @Test
    fun preservesSchoolNameFromCachedProfileOnRefresh() = runTest {
        val storage = FakeKeyValueStorage(
            mapOf(
                "id" to "user-1",
                "name" to "Cached",
                "role" to "PUPIL",
                "school_name" to "Cached School",
            ),
        )
        val repository = repository(storage)

        val result = repository.getCurrentUser(forceRefresh = true)

        assertEquals("Cached School", (result as DomainResult.Success).value.schoolName)
    }

    @Test
    fun preservesSchoolNameFromSessionWhenCacheEmpty() = runTest {
        val repository = repository()

        val result = repository.getCurrentUser()

        assertEquals("Session School", (result as DomainResult.Success).value.schoolName)
    }

    @Test
    fun preservesSchoolNameFromInMemoryUserOnRefresh() = runTest {
        val repository = repository()
        repository.updateCache(
            User.Pupil(
                id = "user-1",
                name = "Mem",
                schoolName = "Memory School",
                pupilClass = "9A",
            ),
        )

        val result = repository.getCurrentUser(forceRefresh = true)

        assertEquals("Memory School", (result as DomainResult.Success).value.schoolName)
    }

    @Test
    fun mapsPupilResponseWithClass() = runTest {
        val repository = repository()

        val result = repository.getCurrentUser()

        val user = (result as DomainResult.Success).value as User.Pupil
        assertEquals("user-1", user.id)
        assertEquals("Fresh", user.name)
        assertEquals("Lee", user.surname)
        assertEquals("9A", user.pupilClass)
        assertEquals(UserRole.PUPIL, user.role)
    }

    @Test
    fun mapsTeacherResponse() = runTest {
        val repository = repository(
            handler = {
                requestCount += 1
                respondJson(pupilResponse(role = "TEACHER"))
            },
        )

        val result = repository.getCurrentUser()

        val user = (result as DomainResult.Success).value as User.Teacher
        assertEquals("Fresh", user.name)
        assertEquals("Session School", user.schoolName)
    }

    @Test
    fun mapsAdminResponseToOrganizator() = runTest {
        val repository = repository(
            handler = {
                requestCount += 1
                respondJson(pupilResponse(role = "ADMIN", pupilClass = null))
            },
        )

        val result = repository.getCurrentUser()

        val user = (result as DomainResult.Success).value
        assertTrue(user is User.Organizator)
        assertEquals("user-1", user.id)
    }

    @Test
    fun mapsHttpUnauthorizedToDomainFailure() = runTest {
        val repository = repository(
            handler = {
                requestCount += 1
                respondJson("""{"message":"expired"}""", HttpStatusCode.Unauthorized)
            },
        )

        val result = repository.getCurrentUser()

        assertEquals(DomainError.Unauthorized, (result as DomainResult.Failure).error)
    }

    @Test
    fun persistsFetchedPupilToStorage() = runTest {
        val storage = FakeKeyValueStorage()
        val repository = repository(storage)

        repository.getCurrentUser()

        assertEquals("user-1", storage.getString("id"))
        assertEquals("Fresh", storage.getString("name"))
        assertEquals("Lee", storage.getString("surname"))
        assertEquals("PUPIL", storage.getString("role"))
        assertEquals("9A", storage.getString("pupil_class"))
    }

    @Test
    fun constructorFallsBackToPupilWhenCachedRoleUnknown() = runTest {
        val storage = FakeKeyValueStorage(
            mapOf(
                "id" to "user-1",
                "name" to "Cached",
                "role" to "GARBAGE",
                "pupil_class" to "8B",
            ),
        )

        val repository = repository(storage)

        val user = repository.currentUser.value
        assertTrue(user is User.Pupil)
        assertEquals("8B", (user as User.Pupil).pupilClass)
    }

    @Test
    fun constructorIgnoresCacheWithoutName() = runTest {
        val storage = FakeKeyValueStorage(mapOf("id" to "user-1"))

        val repository = repository(storage)

        assertNull(repository.currentUser.value)
    }

    @Test
    fun updateCacheReplacesUserAndDropsPupilClassForTeacher() = runTest {
        val storage = FakeKeyValueStorage()
        val firstRepository = repository(storage)
        firstRepository.getCurrentUser()

        val secondRepository = repository(storage)
        secondRepository.updateCache(
            User.Teacher(
                id = "user-1",
                name = "Fresh",
            ),
        )

        val thirdRepository = repository(storage)
        val cached = thirdRepository.currentUser.value
        assertTrue(cached is User.Teacher)
        assertNull(storage.getString("pupil_class"))
    }

    @Test
    fun clearCacheWipesMemoryAndStorage() = runTest {
        val storage = FakeKeyValueStorage()
        val repository = repository(storage)
        repository.getCurrentUser()

        repository.clearCache()

        assertNull(repository.currentUser.value)
        assertTrue(storage.values.isEmpty())
    }
}
