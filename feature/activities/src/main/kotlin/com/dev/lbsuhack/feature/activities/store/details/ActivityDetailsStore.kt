package com.dev.lbsuhack.feature.activities.store.details

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityOrigin
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal

interface ActivityDetailsStore :
    Store<ActivityDetailsStore.Intent, ActivityDetailsStore.State, ActivityDetailsStore.Label> {

    sealed interface Intent {
        data class Load(val activityId: String) : Intent
        data object Vote : Intent
        data object Unvote : Intent
        data object Like : Intent
        data object Dislike : Intent
        data object Unlike : Intent
        data object Delete : Intent
        data object StartPolling : Intent
        data object StopPolling : Intent
        data object Back : Intent
    }

    data class State(
        val activity: ActivityProposal? = null,
        val currentUser: SessionUser? = null,
        val loadState: ActivityLoadState = ActivityLoadState.Idle,
        val error: UiText? = null,
        val errorId: Long = 0L,
        val isDeleting: Boolean = false,
    ) {
        val isLoading: Boolean
            get() = loadState == ActivityLoadState.Loading

        val isAuthor: Boolean
            get() = activity?.isMine == true

        val canVote: Boolean
            get() = currentUser?.role == UserRole.PUPIL

        val canInteract: Boolean
            get() = canVote

        val hasVoted: Boolean
            get() = activity?.hasVoted == true

        val canDelete: Boolean
            get() {
                val current = activity ?: return false
                return when (currentUser?.role) {
                    UserRole.ORGANIZATOR -> true
                    UserRole.TEACHER -> current.isPending && (current.isMine || current.origin == ActivityOrigin.PUPIL)
                    UserRole.PUPIL -> current.isPending && current.isMine
                    null -> false
                }
            }
    }

    sealed interface Label {
        data object Back : Label
        data object Deleted : Label
    }
}

sealed interface ActivityLoadState {
    data object Idle : ActivityLoadState
    data object Loading : ActivityLoadState
    data object NotFound : ActivityLoadState
    data object Forbidden : ActivityLoadState
    data class Failed(val error: UiText) : ActivityLoadState
}
