package com.dev.lbsuhack.feature.profile.store.profile

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.UiText

data class ProfileEditDialog(
    val name: String = "",
    val surname: String = "",
    val fathersName: String = "",
)

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, ProfileStore.Label> {

    sealed interface Intent {
        data object Load : Intent
        data object ToggleNotifications : Intent
        data object ClickEdit : Intent
        data object DismissDialog : Intent
        data class ChangeName(val value: String) : Intent
        data class ChangeSurname(val value: String) : Intent
        data class ChangeFathersName(val value: String) : Intent
        data object ConfirmEdit : Intent
        data object Logout : Intent
    }

    data class State(
        val fullName: String = "",
        val pupilClass: String = "",
        val schoolName: String = "",
        val role: UserRole = UserRole.PUPIL,
        val notificationsEnabled: Boolean = true,
        val savedCount: Int = 0,
        val dialog: ProfileEditDialog? = null,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
    ) {
        val canReviewRequests: Boolean
            get() = role == UserRole.TEACHER || role == UserRole.ORGANIZATOR
    }

    sealed interface Label {
        data class ShowMessage(val text: UiText) : Label
    }
}
