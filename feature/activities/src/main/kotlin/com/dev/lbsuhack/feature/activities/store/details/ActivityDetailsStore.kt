package com.dev.lbsuhack.feature.activities.store.details

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal

interface ActivityDetailsStore :
    Store<ActivityDetailsStore.Intent, ActivityDetailsStore.State, ActivityDetailsStore.Label> {

    sealed interface Intent {
        data class Load(val activityId: String) : Intent
        data object Vote : Intent
        data object Back : Intent
    }

    data class State(
        val activity: ActivityProposal? = null,
        val currentUser: SessionUser? = null,
        val isLoading: Boolean = false,
        val error: UiText? = null,
    ) {
        val isAuthor: Boolean
            get() = currentUser != null && activity?.authorId == currentUser.id

        val hasVoted: Boolean
            get() = activity?.hasVoted == true
    }

    sealed interface Label {
        data object Back : Label
    }
}
