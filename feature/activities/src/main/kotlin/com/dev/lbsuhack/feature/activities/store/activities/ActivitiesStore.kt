package com.dev.lbsuhack.feature.activities.store.activities

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

enum class ActivityTab {
    ALL,
    ON_VOTE,
    ON_ADMIN_MODERATION,
    FINALIZED,
    MY_PROPOSALS,
    MY_VOTES,
}

interface ActivitiesStore : Store<ActivitiesStore.Intent, ActivitiesStore.State, ActivitiesStore.Label> {

    sealed interface Intent {
        data object Refresh : Intent
        data object StartPolling : Intent
        data object StopPolling : Intent
        data class SelectCategory(val category: ActivityCategory?) : Intent
        data class SelectTab(val tab: ActivityTab) : Intent
        data class Vote(val activityId: String) : Intent
        data class Unvote(val activityId: String) : Intent
        data class Like(val activityId: String) : Intent
        data class Unlike(val activityId: String) : Intent
        data class ClickActivity(val activityId: String) : Intent
        data object ClickCreate : Intent
        data object ClickProfile : Intent
    }

    data class State(
        val activities: List<ActivityProposal> = emptyList(),
        val selectedCategory: ActivityCategory? = null,
        val selectedTab: ActivityTab = ActivityTab.ALL,
        val currentUser: SessionUser? = null,
        val isLoading: Boolean = false,
        val hasLoaded: Boolean = false,
        val error: UiText? = null,
        val errorId: Long = 0L,
    ) {
        val filteredActivities: List<ActivityProposal>
            get() = activities.filter { proposal ->
                val matchesCategory = selectedCategory == null || proposal.category == selectedCategory
                val matchesTab = when (selectedTab) {
                    ActivityTab.ALL -> true
                    ActivityTab.ON_VOTE -> proposal.status == ActivityStatus.ON_VOTE
                    ActivityTab.ON_ADMIN_MODERATION -> proposal.status == ActivityStatus.ON_ADMIN_MODERATION
                    ActivityTab.FINALIZED -> proposal.status == ActivityStatus.FINALIZED
                    ActivityTab.MY_PROPOSALS -> proposal.isMine
                    ActivityTab.MY_VOTES -> proposal.hasVoted
                }
                matchesCategory && matchesTab
            }

        val isInitialLoading: Boolean
            get() = activities.isEmpty() && error == null && (!hasLoaded || isLoading)

        val canInteract: Boolean
            get() = currentUser?.role == UserRole.PUPIL

        val canCreate: Boolean
            get() = currentUser != null

        val primaryActionLabel: UiText
            get() = if (canCreate) {
                UiText.Resource(R.string.activities_create_action)
            } else {
                UiText.Resource(R.string.activities_review_action)
            }

        fun isAuthorOf(proposal: ActivityProposal): Boolean = proposal.isMine

        fun canVoteIn(proposal: ActivityProposal): Boolean =
            canInteract && !proposal.isMine && proposal.canChangeVote

        fun canUnvoteIn(proposal: ActivityProposal): Boolean = canInteract && !proposal.isMine

        fun canLikeIn(proposal: ActivityProposal): Boolean =
            canInteract && !proposal.isMine && proposal.canLike
    }

    sealed interface Label {
        data class OpenDetails(val activityId: String) : Label
        data object OpenCreate : Label
        data class OpenRequests(val role: UserRole) : Label
        data object OpenProfile : Label
    }
}
