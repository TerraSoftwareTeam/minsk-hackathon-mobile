package com.dev.lbsuhack.feature.profile.store.profile

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ClearSessionUseCase
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.launch

class ProfileStoreFactory(
    private val storeFactory: StoreFactory,
    private val profileRepository: ProfileRepository,
    private val observeSession: ObserveSessionUseCase,
    private val clearSession: ClearSessionUseCase,
) {

    fun create(): ProfileStore =
        object : ProfileStore,
            Store<ProfileStore.Intent, ProfileStore.State, Nothing> by storeFactory.create(
                name = "ProfileStore",
                initialState = ProfileStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class Loaded(
            val name: String,
            val schoolName: String,
            val pupilClass: String,
            val role: UserRole,
            val notificationsEnabled: Boolean,
            val savedCount: Int,
        ) : Message

        data class NotificationsChanged(val enabled: Boolean) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ProfileStore.Intent, Nothing, ProfileStore.State, Message, Nothing>() {

        override fun executeIntent(intent: ProfileStore.Intent) {
            when (intent) {
                ProfileStore.Intent.Load -> load()
                ProfileStore.Intent.ToggleNotifications -> toggleNotifications()
                ProfileStore.Intent.Logout -> logout()
            }
        }

        private fun load() {
            scope.launch {
                val user = (observeSession.state.value as? SessionState.Authenticated)?.user

                dispatch(
                    Message.Loaded(
                        name = user?.name.orEmpty(),
                        schoolName = user?.schoolName.orEmpty(),
                        pupilClass = user?.pupilClass.orEmpty(),
                        role = user?.role ?: UserRole.PUPIL,
                        notificationsEnabled = profileRepository.isNotificationsEnabled(),
                        savedCount = profileRepository.savedCount(),
                    ),
                )
            }
        }

        private fun toggleNotifications() {
            scope.launch {
                val enabled = !state().notificationsEnabled
                profileRepository.setNotificationsEnabled(enabled)
                dispatch(Message.NotificationsChanged(enabled))
            }
        }

        private fun logout() {
            scope.launch {
                clearSession()
            }
        }
    }

    private object ReducerImpl : Reducer<ProfileStore.State, Message> {
        override fun ProfileStore.State.reduce(msg: Message): ProfileStore.State = when (msg) {
            is Message.Loaded -> copy(
                name = msg.name,
                schoolName = msg.schoolName,
                pupilClass = msg.pupilClass,
                role = msg.role,
                notificationsEnabled = msg.notificationsEnabled,
                savedCount = msg.savedCount,
                isLoading = false,
            )

            is Message.NotificationsChanged -> copy(notificationsEnabled = msg.enabled)
        }
    }
}
