package com.dev.lbsuhack.feature.auth.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.auth.data.AuthRepositoryImpl
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

    private fun createStore(): LoginStore = LoginStoreFactory(
        storeFactory = DefaultStoreFactory(),
        authRepository = AuthRepositoryImpl(),
        saveSession = SaveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun `initial state is empty and not loading`() {
        val store = createStore()

        assertEquals("", store.state.email)
        assertEquals("", store.state.password)
        assertFalse(store.state.isLoading)
        assertNull(store.state.error)
    }

    @Test
    fun `submit with invalid data shows validation error and does not save session`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.EmailChanged("not-an-email"))
            store.accept(LoginStore.Intent.PasswordChanged("123"))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertTrue(store.state.error is UiText.Resource)
            assertNull(sessionRepository.savedUser)
        }

    @Test
    fun `submit with valid data saves session`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.EmailChanged("dev@hackathon.org"))
            store.accept(LoginStore.Intent.PasswordChanged("secret1"))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals("dev@hackathon.org", sessionRepository.savedUser?.email)
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
