package com.dev.lbsuhack.feature.auth.store.login

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.auth.domain.AuthRepository
import com.dev.lbsuhack.feature.auth.toUiText
import kotlinx.coroutines.launch

class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val saveSession: SaveSessionUseCase,
) {

    fun create(): LoginStore =
        object : LoginStore,
            Store<LoginStore.Intent, LoginStore.State, LoginStore.Label> by storeFactory.create(
                name = "LoginStore",
                initialState = LoginStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class EmailChanged(val email: String) : Message
        data class PasswordChanged(val password: String) : Message
        data object Loading : Message
        data object Loaded : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<LoginStore.Intent, Nothing, LoginStore.State, Message, LoginStore.Label>() {

        override fun executeIntent(intent: LoginStore.Intent) {
            when (intent) {
                is LoginStore.Intent.EmailChanged -> dispatch(Message.EmailChanged(intent.email))
                is LoginStore.Intent.PasswordChanged -> dispatch(Message.PasswordChanged(intent.password))
                LoginStore.Intent.OpenRegister -> publish(LoginStore.Label.OpenRegister)
                LoginStore.Intent.Submit -> submit()
            }
        }

        private fun submit() {
            if (state().isLoading) return

            val email = state().email.trim()
            val password = state().password

            if (!email.contains("@") || password.length < MIN_PASSWORD_LENGTH) {
                dispatch(Message.Failed(DomainError.Validation.toUiText()))
                return
            }

            scope.launch {
                dispatch(Message.Loading)

                when (val result = authRepository.login(email, password)) {
                    is DomainResult.Success -> {
                        saveSession(result.value)
                        dispatch(Message.Loaded)
                    }

                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<LoginStore.State, Message> {
        override fun LoginStore.State.reduce(msg: Message): LoginStore.State = when (msg) {
            is Message.EmailChanged -> copy(email = msg.email, error = null)
            is Message.PasswordChanged -> copy(password = msg.password, error = null)
            Message.Loading -> copy(isLoading = true, error = null)
            Message.Loaded -> copy(isLoading = false)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
