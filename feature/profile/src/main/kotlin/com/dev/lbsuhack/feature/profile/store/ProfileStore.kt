package com.dev.lbsuhack.feature.profile.store

import com.arkivanov.mvikotlin.core.store.Store

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, ProfileStore.Label> {

    sealed interface Intent {
        data class SetUsername(val username: String) : Intent
        data object ToggleNotifications : Intent
        data object IncrementSaved : Intent
    }

    data class State(
        val username: String = "HackathonDev",
        val email: String = "dev@hackathon.org",
        val notificationsEnabled: Boolean = true,
        val savedItemsCount: Int = 12,
    )

    sealed interface Label {
        data class NotificationToggled(val enabled: Boolean) : Label
    }
}
