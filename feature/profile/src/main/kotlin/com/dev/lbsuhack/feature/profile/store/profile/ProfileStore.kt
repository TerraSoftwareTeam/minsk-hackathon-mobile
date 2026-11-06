package com.dev.lbsuhack.feature.profile.store.profile

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.model.UserRole

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, Nothing> {

    sealed interface Intent {
        data object Load : Intent
        data object ToggleNotifications : Intent
        data object Logout : Intent
    }

    data class State(
        val name: String = "",
        val schoolName: String = "",
        val pupilClass: String = "",
        val role: UserRole = UserRole.PUPIL,
        val notificationsEnabled: Boolean = true,
        val savedCount: Int = 0,
        val isLoading: Boolean = true,
    ) {
        val canReviewRequests: Boolean
            get() = role == UserRole.TEACHER || role == UserRole.ORGANIZATOR
    }
}
