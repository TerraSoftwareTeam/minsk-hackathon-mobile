package com.dev.lbsuhack.feature.activities.store.create

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory

interface CreateActivityStore :
    Store<CreateActivityStore.Intent, CreateActivityStore.State, CreateActivityStore.Label> {

    sealed interface Intent {
        data class ChangeCategory(val category: ActivityCategory) : Intent
        data class ChangeTitle(val title: String) : Intent
        data class ChangeDescription(val description: String) : Intent
        data class ChangeTargetClass(val targetClass: String) : Intent
        data class ChangeMinParticipants(val min: Int) : Intent
        data class ChangeMaxParticipants(val max: Int) : Intent
        data class ChangePreferences(val preferences: String) : Intent
        data object Submit : Intent
        data object DismissError : Intent
    }

    data class State(
        val category: ActivityCategory = ActivityCategory.BOARD_GAMES,
        val title: String = "",
        val description: String = "",
        val targetClass: String = "8–11 классы",
        val minParticipants: Int = 4,
        val maxParticipants: Int = 12,
        val preferenceNotes: String = "",
        val isSubmitting: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label {
        data class Created(val activityId: String) : Label
    }
}
