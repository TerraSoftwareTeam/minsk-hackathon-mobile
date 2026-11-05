package com.dev.lbsuhack.feature.auth.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.auth.domain.AuthRepository
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.store.login.LoginStoreFactory
import com.dev.lbsuhack.feature.auth.util.FakeSessionRepository
import com.dev.lbsuhack.feature.auth.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = FakeSessionRepository()

    private val fakeAuthRepository = object : AuthRepository {
        var failure: DomainError? = null

        override suspend fun login(
            username: String,
            password: String,
            role: UserRole,
        ): DomainResult<SessionUser> {
            failure?.let { return DomainResult.Failure(it) }
            return if (username == "002" && password == "secret") {
                DomainResult.Success(
                    SessionUser(
                        id = "3575208",
                        name = "Владислав Смирнов",
                        schoolsById = "3575208",
                        role = role,
                        pupilClass = if (role == UserRole.PUPIL) "11 \"М\"" else null,
                    )
                )
            } else {
                DomainResult.Failure(DomainError.Unauthorized)
            }
        }

        override suspend fun register(
            name: String,
            email: String,
            password: String,
        ): DomainResult<SessionUser> = DomainResult.Failure(DomainError.Unknown)
    }

    private fun createStore(): LoginStore = LoginStoreFactory(
        storeFactory = DefaultStoreFactory(),
        authRepository = fakeAuthRepository,
        saveSession = SaveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun `initial state is empty and default role is pupil`() {
        val store = createStore()

        assertEquals("", store.state.username)
        assertEquals("", store.state.password)
        assertEquals(UserRole.PUPIL, store.state.role)
        assertFalse(store.state.isLoading)
        assertNull(store.state.error)
    }

    @Test
    fun `changing role updates state`() {
        val store = createStore()

        store.accept(LoginStore.Intent.RoleChanged(UserRole.TEACHER))
        assertEquals(UserRole.TEACHER, store.state.role)
    }

    @Test
    fun `submit with blank data shows validation error and does not save session`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.UsernameChanged("   "))
            store.accept(LoginStore.Intent.PasswordChanged(""))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertTrue(store.state.error is UiText.Resource)
            assertNull(sessionRepository.savedUser)
        }

    @Test
    fun `submit with valid pupil data saves session with pupil role and class`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.UsernameChanged("002"))
            store.accept(LoginStore.Intent.PasswordChanged("secret"))
            store.accept(LoginStore.Intent.RoleChanged(UserRole.PUPIL))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals("3575208", sessionRepository.savedUser?.schoolsById)
            assertEquals(UserRole.PUPIL, sessionRepository.savedUser?.role)
            assertEquals("11 \"М\"", sessionRepository.savedUser?.pupilClass)
        }

    @Test
    fun `submit with valid teacher data saves session with teacher role and null class`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.UsernameChanged("002"))
            store.accept(LoginStore.Intent.PasswordChanged("secret"))
            store.accept(LoginStore.Intent.RoleChanged(UserRole.TEACHER))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals(UserRole.TEACHER, sessionRepository.savedUser?.role)
            assertNull(sessionRepository.savedUser?.pupilClass)
        }

    @Test
    fun `open register publishes navigation label`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.labels.test {
                store.accept(LoginStore.Intent.OpenRegister)

                assertEquals(LoginStore.Label.OpenRegister, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
