package com.dev.lbsuhack.feature.activities.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityProposalTest {

    private fun createProposal(
        status: ActivityStatus,
        votesCount: Int = 0,
        voteThreshold: Int = 10,
    ): ActivityProposal = ActivityProposal(
        id = "test-1",
        title = "Test Activity",
        description = "Description",
        category = ActivityCategory.SPORTS,
        status = status,
        voteThreshold = voteThreshold,
        votesCount = votesCount,
        hasVoted = false,
        authorName = "Author",
    )

    @Test
    fun reactionSwitchesAdjustScoreWithoutAccumulatingVotes() {
        val transitions = listOf(
            Triple(0, 0, 10), Triple(0, 1, 11), Triple(0, -1, 9),
            Triple(1, 0, 9), Triple(1, 1, 10), Triple(1, -1, 8),
            Triple(-1, 0, 11), Triple(-1, 1, 12), Triple(-1, -1, 10),
        )
        for ((previous, selected, expectedScore) in transitions) {
            val initial = createProposal(ActivityStatus.ON_VOTE).copy(
                likesCount = 10,
                likedByMe = previous == 1,
                dislikedByMe = previous == -1,
            )
            val updated = initial.withReaction(selected)
            assertEquals(expectedScore, updated.likesCount)
            assertEquals(selected == 1, updated.likedByMe)
            assertEquals(selected == -1, updated.dislikedByMe)
        }
    }

    @Test
    fun statusPermissionsTableMatchesSpecification() {
        val onModeration = createProposal(ActivityStatus.ON_MODERATION)
        assertFalse(onModeration.canChangeVote)
        assertFalse(onModeration.canLike)

        val declined = createProposal(ActivityStatus.DECLINED)
        assertFalse(declined.canChangeVote)
        assertFalse(declined.canLike)

        val onVote = createProposal(ActivityStatus.ON_VOTE)
        assertTrue(onVote.canChangeVote)
        assertTrue(onVote.canLike)

        val expired = createProposal(ActivityStatus.EXPIRED)
        assertFalse(expired.canChangeVote)
        assertFalse(expired.canLike)

        val onAdminModeration = createProposal(ActivityStatus.ON_ADMIN_MODERATION)
        assertFalse(onAdminModeration.canChangeVote)
        assertFalse(onAdminModeration.canLike)

        val finalized = createProposal(ActivityStatus.FINALIZED)
        assertFalse(finalized.canChangeVote)
        assertTrue(finalized.canLike)

        val declinedByAdmin = createProposal(ActivityStatus.DECLINED_BY_ADMIN)
        assertFalse(declinedByAdmin.canChangeVote)
        assertTrue(declinedByAdmin.canLike)
    }

    @Test
    fun sharingIsAllowedOnlyForPublishedStatuses() {
        assertFalse(createProposal(ActivityStatus.ON_MODERATION).canShare)
        assertFalse(createProposal(ActivityStatus.DECLINED).canShare)
        assertFalse(createProposal(ActivityStatus.DECLINED_BY_ADMIN).canShare)
        assertFalse(createProposal(ActivityStatus.EXPIRED).canShare)
        assertFalse(createProposal(ActivityStatus.ON_ADMIN_MODERATION).canShare)
        assertTrue(createProposal(ActivityStatus.ON_VOTE).canShare)
        assertTrue(createProposal(ActivityStatus.FINALIZED).canShare)
    }

    @Test
    fun isQuorumReachedComputesCorrectly() {
        assertFalse(createProposal(ActivityStatus.ON_VOTE, votesCount = 4, voteThreshold = 5).isQuorumReached)
        assertTrue(createProposal(ActivityStatus.ON_VOTE, votesCount = 5, voteThreshold = 5).isQuorumReached)
        assertTrue(createProposal(ActivityStatus.ON_VOTE, votesCount = 6, voteThreshold = 5).isQuorumReached)
    }

    @Test
    fun progressComputesCorrectly() {
        assertEquals(0f, createProposal(ActivityStatus.ON_VOTE, votesCount = 0, voteThreshold = 0).progress, 0.001f)
        assertEquals(0.5f, createProposal(ActivityStatus.ON_VOTE, votesCount = 5, voteThreshold = 10).progress, 0.001f)
        assertEquals(1f, createProposal(ActivityStatus.ON_VOTE, votesCount = 15, voteThreshold = 10).progress, 0.001f)
    }
}
