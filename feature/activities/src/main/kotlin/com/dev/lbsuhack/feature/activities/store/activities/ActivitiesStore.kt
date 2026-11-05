package com.dev.lbsuhack.feature.activities.store.activities

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

enum class ActivityTab {
    ALL,
    RECRUITING,
    PENDING_ADMIN,
    APPROVED,
    MY_PROPOSALS,
    MY_PARTICIPATIONS,
}

interface ActivitiesStore : Store<ActivitiesStore.Intent, ActivitiesStore.State, ActivitiesStore.Label> {

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data class SelectCategory(val category: ActivityCategory?) : Intent
        data class SelectTab(val tab: ActivityTab) : Intent
        data class ToggleJoin(val activityId: String) : Intent
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
                    ActivityTab.RECRUITING -> proposal.status == ActivityStatus.RECRUITING
                    ActivityTab.PENDING_ADMIN -> proposal.status == ActivityStatus.PENDING_ADMIN
                    ActivityTab.APPROVED -> proposal.status == ActivityStatus.APPROVED
                    ActivityTab.MY_PROPOSALS -> currentUser != null && proposal.authorId == currentUser.id
                    ActivityTab.MY_PARTICIPATIONS -> currentUser != null && proposal.isJoined(currentUser.id)
                }
                matchesCategory && matchesTab
            }

        val myProposalsCount: Int
            get() = if (currentUser == null) 0 else activities.count { it.authorId == currentUser.id }

        val myParticipationsCount: Int
            get() = if (currentUser == null) 0 else activities.count { it.isJoined(currentUser.id) }
    }

    sealed interface Label {
        data class OpenDetails(val activityId: String) : Label
        data object OpenCreate : Label
        data object OpenProfile : Label
    }
}
