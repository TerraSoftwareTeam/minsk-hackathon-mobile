package com.dev.lbsuhack.feature.auth.store.register

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

class RegisterStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository,
    private val saveSession: SaveSessionUseCase,
) {

    fun create(): RegisterStore =
        object : RegisterStore,
            Store<RegisterStore.Intent, RegisterStore.State, RegisterStore.Label> by storeFactory.create(
                name = "RegisterStore",
                initialState = RegisterStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class NameChanged(val name: String) : Message
        data class EmailChanged(val email: String) : Message
        data class PasswordChanged(val password: String) : Message
        data object Loading : Message
        data object Loaded : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RegisterStore.Intent, Nothing, RegisterStore.State, Message, RegisterStore.Label>() {

        override fun executeIntent(intent: RegisterStore.Intent) {
            when (intent) {
                is RegisterStore.Intent.NameChanged -> dispatch(Message.NameChanged(intent.name))
                is RegisterStore.Intent.EmailChanged -> dispatch(Message.EmailChanged(intent.email))
                is RegisterStore.Intent.PasswordChanged -> dispatch(Message.PasswordChanged(intent.password))
                RegisterStore.Intent.BackToLogin -> publish(RegisterStore.Label.BackToLogin)
                RegisterStore.Intent.Submit -> submit()
            }
        }

        private fun submit() {
            if (state().isLoading) return

            val name = state().name.trim()
            val email = state().email.trim()
            val password = state().password

            if (
                name.isBlank() ||
                !email.contains("@") ||
                password.length < MIN_PASSWORD_LENGTH
            ) {
                dispatch(Message.Failed(DomainError.Validation.toUiText()))
                return
            }

            scope.launch {
                dispatch(Message.Loading)

                when (val result = authRepository.register(name, email, password)) {
                    is DomainResult.Success -> {
                        saveSession(result.value)
                        dispatch(Message.Loaded)
                    }

                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RegisterStore.State, Message> {
        override fun RegisterStore.State.reduce(msg: Message): RegisterStore.State = when (msg) {
            is Message.NameChanged -> copy(name = msg.name, error = null)
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
