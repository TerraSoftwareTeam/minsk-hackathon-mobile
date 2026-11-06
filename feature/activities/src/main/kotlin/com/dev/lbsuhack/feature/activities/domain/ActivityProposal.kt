package com.dev.lbsuhack.feature.activities.domain

data class ActivityProposal(
    val id: String,
    val title: String,
    val description: String,
    val category: ActivityCategory,
    val scope: ActivityScope = ActivityScope.CLASS,
    val status: ActivityStatus,
    val voteThreshold: Int,
    val votesCount: Int,
    val hasVoted: Boolean,
    val startsAt: String,
    val votingClosesAt: String,
    val authorId: String,
    val authorName: String,
    val authorClass: String,
    val teacherName: String? = null,
    val declineReason: String? = null,
) {
    val progress: Float
        get() = if (voteThreshold > 0) (votesCount.toFloat() / voteThreshold).coerceIn(0f, 1f) else 0f

    val isQuorumReached: Boolean
        get() = votesCount >= voteThreshold
}
