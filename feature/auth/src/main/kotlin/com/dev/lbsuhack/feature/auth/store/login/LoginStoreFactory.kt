package com.dev.lbsuhack.feature.auth.store.login

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.domain.AuthRepository
import com.dev.lbsuhack.feature.auth.parseSchoolClass
import com.dev.lbsuhack.feature.auth.toUiText
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.random.Random

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
        data class UsernameChanged(val username: String) : Message
        data class PasswordChanged(val password: String) : Message
        data class SurnameChanged(val surname: String) : Message
        data class NameChanged(val name: String) : Message
        data class ClassNameChanged(val className: String) : Message
        data class RoleChanged(val role: UserRole) : Message
        data class PupilMethodChanged(val method: PupilLoginMethod) : Message
        data object Loading : Message
        data object Loaded : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<LoginStore.Intent, Nothing, LoginStore.State, Message, LoginStore.Label>() {

        override fun executeIntent(intent: LoginStore.Intent) {
            when (intent) {
                is LoginStore.Intent.UsernameChanged -> dispatch(Message.UsernameChanged(intent.username))
                is LoginStore.Intent.PasswordChanged -> dispatch(Message.PasswordChanged(intent.password))
                is LoginStore.Intent.SurnameChanged -> dispatch(Message.SurnameChanged(intent.surname))
                is LoginStore.Intent.NameChanged -> dispatch(Message.NameChanged(intent.name))
                is LoginStore.Intent.ClassNameChanged -> dispatch(Message.ClassNameChanged(intent.className))
                is LoginStore.Intent.RoleChanged -> dispatch(Message.RoleChanged(intent.role))
                is LoginStore.Intent.PupilMethodChanged -> {
                    if (!state().isLoading) dispatch(Message.PupilMethodChanged(intent.method))
                }
                LoginStore.Intent.Submit -> submit()
            }
        }

        private fun submit() {
            if (state().isLoading) return

            when (state().role) {
                UserRole.ORGANIZATOR -> authenticate {
                    authRepository.registerOrganizer(randomSchoolsById())
                }

                UserRole.TEACHER -> submitTeacher()

                UserRole.PUPIL -> submitPupil()
            }
        }

        private fun submitTeacher() {
            val identity = manualIdentity() ?: return
            authenticate {
                authRepository.registerTeacher(
                    schoolsById = randomSchoolsById(),
                    name = identity.name,
                    surname = identity.surname,
                    className = identity.className,
                )
            }
        }

        private fun submitPupil() {
            when (state().pupilMethod) {
                PupilLoginMethod.SCHOOLS_BY -> submitPupilSchoolsBy()
                PupilLoginMethod.MANUAL -> submitPupilManually()
            }
        }

        private fun submitPupilSchoolsBy() {
            val username = state().username.trim()
            val password = state().password

            if (username.isBlank() || password.isBlank()) {
                dispatch(Message.Failed(DomainError.Validation.toUiText()))
                return
            }

            authenticate { authRepository.login(username, password, UserRole.PUPIL) }
        }

        private fun submitPupilManually() {
            val identity = manualIdentity() ?: return
            authenticate {
                authRepository.registerPupil(
                    schoolsById = manualPupilSchoolsById(identity),
                    name = identity.name,
                    surname = identity.surname,
                    className = identity.className,
                )
            }
        }

        private fun manualIdentity(): ManualIdentity? {
            val surname = state().surname.trim()
            val name = state().name.trim()

            if (surname.isBlank() || name.isBlank()) {
                dispatch(Message.Failed(UiText.Resource(R.string.auth_error_teacher_validation)))
                return null
            }
            val className = parseSchoolClass(state().className) ?: run {
                dispatch(Message.Failed(UiText.Resource(R.string.auth_error_class_format)))
                return null
            }
            return ManualIdentity(name = name, surname = surname, className = className)
        }

        private fun authenticate(block: suspend () -> DomainResult<SessionUser>) {
            scope.launch {
                dispatch(Message.Loading)

                when (val result = block()) {
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
            is Message.UsernameChanged -> copy(username = msg.username, error = null)
            is Message.PasswordChanged -> copy(password = msg.password, error = null)
            is Message.SurnameChanged -> copy(surname = msg.surname, error = null)
            is Message.NameChanged -> copy(name = msg.name, error = null)
            is Message.ClassNameChanged -> copy(className = msg.className, error = null)
            is Message.RoleChanged -> copy(role = msg.role, error = null)
            is Message.PupilMethodChanged -> copy(pupilMethod = msg.method, error = null)
            Message.Loading -> copy(isLoading = true, error = null)
            Message.Loaded -> LoginStore.State(role = role, pupilMethod = pupilMethod)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
        }
    }

    private data class ManualIdentity(
        val name: String,
        val surname: String,
        val className: String,
    )

    private companion object {
        fun randomSchoolsById(): String = Random.nextInt(1_000_000, 10_000_000).toString()

        fun manualPupilSchoolsById(identity: ManualIdentity): String {
            val key = listOf(identity.surname, identity.name, identity.className)
                .joinToString(separator = "|") { it.lowercase() }
            val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
            val value = digest.take(MANUAL_ID_BYTES).fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
            return (MANUAL_ID_MIN + Math.floorMod(value, MANUAL_ID_RANGE)).toString()
        }

        const val MANUAL_ID_BYTES = 7
        const val MANUAL_ID_MIN = 100_000_000_000L
        const val MANUAL_ID_RANGE = 900_000_000_000L
    }
}
