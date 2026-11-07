package com.dev.lbsuhack.feature.profile.store.profile

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.push.ObserveNotificationsEnabledUseCase
import com.dev.lbsuhack.core.domain.push.SetNotificationsEnabledUseCase
import com.dev.lbsuhack.core.domain.session.ClearSessionUseCase
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.domain.user.GetCurrentUserUseCase
import com.dev.lbsuhack.core.domain.user.UpdateUserCacheUseCase
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import com.dev.lbsuhack.feature.profile.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProfileStoreFactory(
    private val storeFactory: StoreFactory,
    private val profileRepository: ProfileRepository,
    private val observeNotificationsEnabled: ObserveNotificationsEnabledUseCase,
    private val setNotificationsEnabled: SetNotificationsEnabledUseCase,
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
        data object LoadingStarted : Message
        data object LoadingFinished : Message
        data object LoadingFailed : Message
        data object Saving : Message
        data object SavingFinished : Message
        data object SavingSucceeded : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ProfileStore.Intent, Nothing, ProfileStore.State, Message, ProfileStore.Label>() {

        private var loadJob: Job? = null
        private var editJob: Job? = null
        private val toggleMutex = Mutex()

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
            loadJob?.cancel()
            loadJob = scope.launch {
                dispatch(Message.LoadingStarted)
                val initialNotifications = if (state().hasUser) state().notificationsEnabled else observeNotificationsEnabled().first()
                val initialSaved = if (state().hasUser) state().savedCount else profileRepository.savedCount()
                val cached = (getCurrentUser(forceRefresh = false) as? DomainResult.Success)?.value
                if (cached != null) {
                    dispatch(
                        Message.UserLoaded(
                            fullName = cached.displayName(),
                            pupilClass = (cached as? User.Pupil)?.pupilClass.orEmpty(),
                            schoolName = cached.schoolName.orEmpty(),
                            role = cached.role,
                            notificationsEnabled = initialNotifications,
                            savedCount = initialSaved,
                        ),
                    )
                }
                when (val result = getCurrentUser(forceRefresh = true)) {
                    is DomainResult.Success -> {
                        val user = result.value
                        val currentNotifications = if (state().hasUser) state().notificationsEnabled else observeNotificationsEnabled().first()
                        val currentSaved = if (state().hasUser) state().savedCount else profileRepository.savedCount()
                        dispatch(
                            Message.UserLoaded(
                                fullName = user.displayName(),
                                pupilClass = (user as? User.Pupil)?.pupilClass.orEmpty(),
                                schoolName = user.schoolName.orEmpty(),
                                role = user.role,
                                notificationsEnabled = currentNotifications,
                                savedCount = currentSaved,
                            ),
                        )
                    }

                    is DomainResult.Failure -> {
                        if (!state().hasUser) {
                            dispatch(Message.LoadingFailed)
                            publish(ProfileStore.Label.ShowMessage(result.error.toUiText()))
                        } else {
                            dispatch(Message.LoadingFinished)
                        }
                    }
                }
            }
        }

        private fun toggleNotifications() {
            dispatch(Message.NotificationsChanged(!state().notificationsEnabled))
            scope.launch {
                toggleMutex.withLock {
                    val target = state().notificationsEnabled
                    if (setNotificationsEnabled(target)) return@withLock
                    if (state().notificationsEnabled == target) {
                        dispatch(Message.NotificationsChanged(!target))
                        publish(ProfileStore.Label.ShowMessage(UiText.Resource(R.string.profile_notifications_error)))
                    }
                }
            }
        }

        private fun openEditDialog() {
            if (state().dialog != null || editJob?.isActive == true) return

            editJob = scope.launch {
                val user = (getCurrentUser() as? DomainResult.Success)?.value
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
                if (previousUser == null || userId.isNullOrBlank()) {
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
                            is User.Pupil -> updated.copy(schoolName = previousUser.schoolName)
                            is User.Teacher -> updated.copy(schoolName = previousUser.schoolName)
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
                        dispatch(Message.SavingSucceeded)
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
                isError = false,
                hasUser = true,
            )

            is Message.NotificationsChanged -> copy(notificationsEnabled = msg.enabled)
            is Message.DialogOpened -> copy(dialog = msg.dialog, isSaving = false)
            is Message.NameChanged -> copy(dialog = dialog?.copy(name = msg.value))
            is Message.SurnameChanged -> copy(dialog = dialog?.copy(surname = msg.value))
            Message.LoadingStarted -> copy(isLoading = !hasUser, isError = false)
            Message.LoadingFinished -> copy(isLoading = false)
            Message.LoadingFailed -> copy(isLoading = false, isError = true)
            Message.Saving -> copy(isSaving = true)
            Message.SavingFinished -> copy(isSaving = false)
            Message.SavingSucceeded -> copy(dialog = null, isSaving = false)
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
