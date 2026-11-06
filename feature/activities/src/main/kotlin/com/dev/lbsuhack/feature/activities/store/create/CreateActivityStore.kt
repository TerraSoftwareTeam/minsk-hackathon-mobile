package com.dev.lbsuhack.feature.activities.store.create

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityScope

interface CreateActivityStore :
    Store<CreateActivityStore.Intent, CreateActivityStore.State, CreateActivityStore.Label> {

    sealed interface Intent {
        data class ChangeCategory(val category: ActivityCategory) : Intent
        data class ChangeTitle(val title: String) : Intent
        data class ChangeDescription(val description: String) : Intent
        data class ChangeScope(val scope: ActivityScope) : Intent
        data class ChangeVoteThreshold(val threshold: Int) : Intent
        data class ChangeStartsAt(val startsAt: String) : Intent
        data object Submit : Intent
        data object DismissError : Intent
    }

    data class State(
        val category: ActivityCategory = ActivityCategory.SPORTS,
        val title: String = "",
        val description: String = "",
        val scope: ActivityScope = ActivityScope.CLASS,
        val voteThreshold: Int = 15,
        val startsAt: String = "2026-11-21T10:00:00.000Z",
        val isSubmitting: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label {
        data class Created(val activityId: String) : Label
    }
}
