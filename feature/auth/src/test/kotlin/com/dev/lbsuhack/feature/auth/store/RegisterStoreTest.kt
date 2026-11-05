package com.dev.lbsuhack.feature.auth.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.feature.auth.data.AuthRepositoryImpl
import com.dev.lbsuhack.feature.auth.store.register.RegisterStore
import com.dev.lbsuhack.feature.auth.store.register.RegisterStoreFactory
import com.dev.lbsuhack.feature.auth.util.FakeSessionRepository
import com.dev.lbsuhack.feature.auth.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = FakeSessionRepository()

    private fun createStore(): RegisterStore = RegisterStoreFactory(
        storeFactory = DefaultStoreFactory(),
        authRepository = AuthRepositoryImpl(),
        saveSession = SaveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun `register creates and saves a session`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(RegisterStore.Intent.NameChanged("Nikita"))
            store.accept(RegisterStore.Intent.EmailChanged("nikita@example.com"))
            store.accept(RegisterStore.Intent.PasswordChanged("secret1"))
            store.accept(RegisterStore.Intent.Submit)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals("Nikita", sessionRepository.savedUser?.name)
        }

    @Test
    fun `back publishes navigation label`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.labels.test {
                store.accept(RegisterStore.Intent.BackToLogin)

                assertEquals(RegisterStore.Label.BackToLogin, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
