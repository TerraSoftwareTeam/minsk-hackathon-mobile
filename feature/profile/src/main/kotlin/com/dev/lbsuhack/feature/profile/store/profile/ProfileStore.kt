package com.dev.lbsuhack.feature.profile.store.profile

import com.arkivanov.mvikotlin.core.store.Store

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, Nothing> {

    sealed interface Intent {
        data object Load : Intent
        data object ToggleNotifications : Intent
        data object IncrementSaved : Intent
        data object Logout : Intent
    }

    data class State(
        val name: String = "",
        val email: String = "",
        val notificationsEnabled: Boolean = true,
        val savedCount: Int = 0,
        val isLoading: Boolean = true,
    )
}
