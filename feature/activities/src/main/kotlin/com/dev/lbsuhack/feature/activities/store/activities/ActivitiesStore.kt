package com.dev.lbsuhack.feature.activities.store.activities

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
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
        data object Load : Intent
        data object Refresh : Intent
        data class SelectCategory(val category: ActivityCategory?) : Intent
        data class SelectTab(val tab: ActivityTab) : Intent
        data class Vote(val activityId: String) : Intent
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
        val error: UiText? = null,
    ) {
        val filteredActivities: List<ActivityProposal>
            get() = activities.filter { proposal ->
                val matchesCategory = selectedCategory == null || proposal.category == selectedCategory
                val matchesTab = when (selectedTab) {
                    ActivityTab.ALL -> true
                    ActivityTab.ON_VOTE -> proposal.status == ActivityStatus.ON_VOTE
                    ActivityTab.ON_ADMIN_MODERATION -> proposal.status == ActivityStatus.ON_ADMIN_MODERATION
                    ActivityTab.FINALIZED -> proposal.status == ActivityStatus.FINALIZED
                    ActivityTab.MY_PROPOSALS -> proposal.isCreatedBy(currentUser?.schoolsById)
                    ActivityTab.MY_VOTES -> proposal.hasVoted
                }
                matchesCategory && matchesTab
            }

        val myProposalsCount: Int
            get() = if (currentUser == null) 0 else activities.count { it.isCreatedBy(currentUser.schoolsById) }

        val myVotesCount: Int
            get() = activities.count { it.hasVoted }
    }

    sealed interface Label {
        data class OpenDetails(val activityId: String) : Label
        data object OpenCreate : Label
        data object OpenProfile : Label
    }
}
