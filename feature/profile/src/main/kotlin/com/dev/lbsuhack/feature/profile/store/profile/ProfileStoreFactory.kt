package com.dev.lbsuhack.feature.profile.store.profile

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.domain.session.ClearSessionUseCase
import com.dev.lbsuhack.core.domain.user.GetCurrentUserUseCase
import com.dev.lbsuhack.core.domain.user.UpdateUserCacheUseCase
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.profile.toUiText
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileStoreFactory(
    private val storeFactory: StoreFactory,
    private val profileRepository: ProfileRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val updateUserCache: UpdateUserCacheUseCase,
    private val saveSession: SaveSessionUseCase,
    private val clearSession: ClearSessionUseCase,
) {

    fun create(): ProfileStore =
        object : ProfileStore,
            Store<ProfileStore.Intent, ProfileStore.State, ProfileStore.Label> by storeFactory.create(
                name = "ProfileStore",
                initialState = ProfileStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class UserLoaded(
            val fullName: String,
            val pupilClass: String,
            val schoolName: String,
            val role: UserRole,
            val notificationsEnabled: Boolean,
            val savedCount: Int,
        ) : Message

        data class NotificationsChanged(val enabled: Boolean) : Message
        data class DialogOpened(val dialog: ProfileEditDialog?) : Message
        data class NameChanged(val value: String) : Message
        data class SurnameChanged(val value: String) : Message
        data object Saving : Message
        data object SavingFinished : Message
        data object LoadingFinished : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ProfileStore.Intent, Nothing, ProfileStore.State, Message, ProfileStore.Label>() {

        private var loading = false

        override fun executeIntent(intent: ProfileStore.Intent) {
            when (intent) {
                ProfileStore.Intent.Load -> load()
                ProfileStore.Intent.ToggleNotifications -> toggleNotifications()
                ProfileStore.Intent.ClickEdit -> openEditDialog()
                ProfileStore.Intent.DismissDialog -> dispatch(Message.DialogOpened(null))
                is ProfileStore.Intent.ChangeName -> dispatch(Message.NameChanged(intent.value))
                is ProfileStore.Intent.ChangeSurname -> dispatch(Message.SurnameChanged(intent.value))
                ProfileStore.Intent.ConfirmEdit -> confirmEdit()
                ProfileStore.Intent.Logout -> logout()
            }
        }

        private fun load() {
            scope.launch {
                val notificationsEnabled = profileRepository.isNotificationsEnabled()
                val savedCount = profileRepository.savedCount()
                val cached = (getCurrentUser() as? DomainResult.Success)?.value
                if (cached != null) {
                    dispatch(Message.UserLoaded(
                        fullName = cached.displayName(),
                        pupilClass = (cached as? User.Pupil)?.pupilClass.orEmpty(),
                        schoolName = cached.schoolName.orEmpty(),
                        role = cached.role,
                        notificationsEnabled = notificationsEnabled,
                        savedCount = savedCount,
                    ))
                }
                when (val result = getCurrentUser(forceRefresh = true)) {
                    is DomainResult.Success -> {
                        val user = result.value
                        dispatch(
                            Message.UserLoaded(
                                fullName = user.displayName(),
                                pupilClass = (user as? User.Pupil)?.pupilClass.orEmpty(),
                                schoolName = user.schoolName.orEmpty(),
                                role = user.role,
                                notificationsEnabled = notificationsEnabled,
                                savedCount = savedCount,
                            ),
                        )
                    }

                    is DomainResult.Failure -> {
                        dispatch(Message.LoadingFinished)
                        publish(ProfileStore.Label.ShowMessage(result.error.toUiText()))
                    }
                }
            }
        }

        private fun toggleNotifications() {
            scope.launch {
                val enabled = !state().notificationsEnabled
                profileRepository.setNotificationsEnabled(enabled)
                dispatch(Message.NotificationsChanged(enabled))
            }
        }

        private fun openEditDialog() {
            val dialog = state().dialog
            if (dialog != null) return

            scope.launch {
                val user = getCurrentUser().let { (it as? DomainResult.Success)?.value }
                dispatch(
                    Message.DialogOpened(
                        ProfileEditDialog(
                            name = user?.name.orEmpty(),
                            surname = user?.surname.orEmpty(),
                        ),
                    ),
                )
            }
        }

        private fun confirmEdit() {
            val dialog = state().dialog ?: return
            if (state().isSaving) return

            if (dialog.name.isBlank() || dialog.surname.isBlank()) {
                publish(ProfileStore.Label.ShowMessage(UiText.Resource(R.string.profile_edit_error_fields_required)))
                return
            }

            dispatch(Message.Saving)
            scope.launch {
                val previousUser = (getCurrentUser() as? DomainResult.Success)?.value
                val userId = previousUser?.id
                if (userId.isNullOrBlank()) {
                    dispatch(Message.SavingFinished)
                    publish(ProfileStore.Label.ShowMessage(UiText.Resource(R.string.profile_error_unknown)))
                    return@launch
                }

                when (
                    val result = profileRepository.updateOwnProfile(
                        userId = userId,
                        name = dialog.name,
                        surname = dialog.surname,
                    )
                ) {
                    is DomainResult.Success -> {
                        val user = when (val updated = result.value) {
                            is User.Pupil -> updated.copy(schoolName = previousUser?.schoolName)
                            is User.Teacher -> updated.copy(schoolName = previousUser?.schoolName)
                            is User.Organizator -> updated
                        }
                        updateUserCache(user)
                        saveSession(user.toSessionUser())
                        dispatch(
                            Message.UserLoaded(
                                fullName = user.displayName(),
                                pupilClass = (user as? User.Pupil)?.pupilClass.orEmpty(),
                                schoolName = user.schoolName.orEmpty(),
                                role = user.role,
                                notificationsEnabled = state().notificationsEnabled,
                                savedCount = state().savedCount,
                            ),
                        )
                        dispatch(Message.SavingFinished)
                        publish(ProfileStore.Label.ShowMessage(UiText.Resource(R.string.profile_success_updated)))
                    }

                    is DomainResult.Failure -> {
                        dispatch(Message.SavingFinished)
                        publish(ProfileStore.Label.ShowMessage(result.error.toUiText()))
                    }
                }
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
            is Message.UserLoaded -> copy(
                fullName = msg.fullName,
                pupilClass = msg.pupilClass,
                schoolName = msg.schoolName,
                role = msg.role,
                notificationsEnabled = msg.notificationsEnabled,
                savedCount = msg.savedCount,
                isLoading = false,
            )

            is Message.NotificationsChanged -> copy(notificationsEnabled = msg.enabled)
            is Message.DialogOpened -> copy(dialog = msg.dialog, isSaving = false)
            is Message.NameChanged -> copy(dialog = dialog?.copy(name = msg.value))
            is Message.SurnameChanged -> copy(dialog = dialog?.copy(surname = msg.value))
            Message.Saving -> copy(isSaving = true)
            Message.SavingFinished -> copy(isSaving = false)
            Message.LoadingFinished -> copy(isLoading = false)
        }
    }

    private fun User.displayName(): String = listOfNotNull(
        name.takeIf { it.isNotBlank() },
        surname?.takeIf { it.isNotBlank() },
    ).joinToString(" ")

    private fun User.toSessionUser(): SessionUser = SessionUser(
        id = id,
        name = name,
        surname = surname,
        schoolsById = schoolsById,
        schoolName = schoolName,
        role = role,
        pupilClass = (this as? User.Pupil)?.pupilClass,
    )
}
