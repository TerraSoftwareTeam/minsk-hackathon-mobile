package com.dev.lbsuhack.feature.activities.store

import com.dev.lbsuhack.feature.activities.domain.ActivityStage
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.domain.stage
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import com.dev.lbsuhack.feature.activities.util.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityFeedSectionsTest {

    private fun proposal(id: String, status: ActivityStatus) =
        TestFixtures.otherProposal.copy(id = id, status = status)

    @Test
    fun everyStatusBelongsToAStage() {
        assertEquals(ActivityStage.VOTING, ActivityStatus.ON_VOTE.stage)
        assertEquals(ActivityStage.WAITING, ActivityStatus.ON_MODERATION.stage)
        assertEquals(ActivityStage.WAITING, ActivityStatus.ON_ADMIN_MODERATION.stage)
        assertEquals(ActivityStage.CONFIRMED, ActivityStatus.FINALIZED.stage)
        assertEquals(ActivityStage.CLOSED, ActivityStatus.DECLINED.stage)
        assertEquals(ActivityStage.CLOSED, ActivityStatus.DECLINED_BY_ADMIN.stage)
        assertEquals(ActivityStage.CLOSED, ActivityStatus.EXPIRED.stage)
    }

    @Test
    fun feedIsGroupedWithVotingFirstAndKeepsOrderInsideSections() {
        val state = ActivitiesStore.State(
            activities = listOf(
                proposal("declined", ActivityStatus.DECLINED),
                proposal("moderation", ActivityStatus.ON_MODERATION),
                proposal("vote-1", ActivityStatus.ON_VOTE),
                proposal("final", ActivityStatus.FINALIZED),
                proposal("admin", ActivityStatus.ON_ADMIN_MODERATION),
                proposal("vote-2", ActivityStatus.ON_VOTE),
            ),
        )

        val sections = state.feedSections

        assertEquals(
            listOf(ActivityStage.VOTING, ActivityStage.CONFIRMED, ActivityStage.WAITING, ActivityStage.CLOSED),
            sections.map { it.stage },
        )
        assertEquals(listOf("vote-1", "vote-2"), sections[0].activities.map { it.id })
        assertEquals(listOf("moderation", "admin"), sections[2].activities.map { it.id })
    }

    @Test
    fun emptySectionsAreSkippedAndFiltersApply() {
        val state = ActivitiesStore.State(
            activities = listOf(
                proposal("vote", ActivityStatus.ON_VOTE),
                proposal("final", ActivityStatus.FINALIZED),
            ),
            selectedTab = ActivityTab.ON_VOTE,
        )

        assertEquals(listOf(ActivityStage.VOTING), state.feedSections.map { it.stage })
    }

    @Test
    fun quickFiltersAreAllMyVotesAndMyProposals() {
        assertEquals(
            listOf(ActivityTab.ALL, ActivityTab.MY_VOTES, ActivityTab.MY_PROPOSALS),
            com.dev.lbsuhack.feature.activities.store.activities.QuickFilterTabs,
        )
        assertEquals(false, ActivitiesStore.State().hasActiveFilters)
        assertEquals(true, ActivitiesStore.State(selectedTab = ActivityTab.MY_VOTES).hasActiveFilters)
    }

    @Test
    fun myVotesHidesClosedEvents() {
        val state = ActivitiesStore.State(
            activities = listOf(
                proposal("going", ActivityStatus.ON_VOTE).copy(hasVoted = true),
                proposal("declined", ActivityStatus.DECLINED).copy(hasVoted = true),
                proposal("other", ActivityStatus.ON_VOTE).copy(hasVoted = false),
            ),
            selectedTab = ActivityTab.MY_VOTES,
        )

        assertEquals(listOf("going"), state.filteredActivities.map { it.id })
    }
}
