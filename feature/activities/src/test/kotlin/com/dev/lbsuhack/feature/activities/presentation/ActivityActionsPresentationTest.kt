package com.dev.lbsuhack.feature.activities.presentation

import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityActionsPresentationTest {
    private fun proposal(score: Int, status: ActivityStatus = ActivityStatus.ON_VOTE, isMine: Boolean = false) = ActivityProposal(
        id = "event", title = "Event", description = "", category = ActivityCategory.SPORTS,
        status = status, voteThreshold = 5, votesCount = 2, hasVoted = false,
        authorName = "Organizer", likesCount = score, isMine = isMine,
    )

    @Test
    fun staffSeeOnlyNonzeroReadOnlyRatingsRegardlessOfAuthorship() {
        for (role in listOf(UserRole.TEACHER, UserRole.ORGANIZATOR)) {
            for (status in listOf(ActivityStatus.ON_VOTE, ActivityStatus.FINALIZED, ActivityStatus.DECLINED_BY_ADMIN)) {
                for (isMine in listOf(false, true)) {
                    for (score in listOf(-3, 0, 12)) {
                        val actions = proposal(score, status, isMine).actionsFor(role)
                        assertFalse(actions.showParticipation)
                        assertEquals(score != 0, actions.isVisible)
                        assertEquals(if (score == 0) ActivityRatingMode.HIDDEN else ActivityRatingMode.SUMMARY, actions.ratingMode)
                    }
                }
            }
        }
    }

    @Test
    fun pupilsKeepInteractiveZeroRatingAndParticipation() {
        val actions = proposal(0).actionsFor(UserRole.PUPIL)
        assertEquals(ActivityRatingMode.INTERACTIVE, actions.ratingMode)
        assertTrue(actions.showParticipation)
        assertTrue(actions.isVisible)
    }

    @Test
    fun moderationHidesRatingForEveryRoleEvenWhenScoreIsNonzero() {
        for (role in UserRole.entries) {
            for (status in listOf(ActivityStatus.ON_MODERATION, ActivityStatus.ON_ADMIN_MODERATION)) {
                for (score in listOf(-3, 0, 12)) {
                    val actions = proposal(score, status).actionsFor(role)
                    assertEquals(ActivityRatingMode.HIDDEN, actions.ratingMode)
                    assertEquals(role == UserRole.PUPIL, actions.isVisible)
                }
            }
        }
        assertFalse(proposal(12).actionsFor(null).isVisible)
    }
}
