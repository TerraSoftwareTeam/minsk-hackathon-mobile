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
    val startsAt: String = "",
    val votingClosesAt: String = "",
    val createdAt: String = "",
    val authorName: String,
    val teacherName: String? = null,
    val declineReason: String? = null,
    val photoUri: String? = null,
    val confirmedTime: String? = null,
    val voters: List<String> = emptyList(),
    val likesCount: Int = 0,
    val likedByMe: Boolean = false,
    val isMine: Boolean = false,
) {
    val isQuorumReached: Boolean get() = votesCount >= voteThreshold

    val progress: Float
        get() = if (voteThreshold > 0) (votesCount.toFloat() / voteThreshold).coerceIn(0f, 1f) else 0f

    val canChangeVote: Boolean get() = status == ActivityStatus.ON_VOTE

    val canLike: Boolean
        get() = status == ActivityStatus.ON_VOTE || status == ActivityStatus.ON_ADMIN_MODERATION ||
            status == ActivityStatus.FINALIZED || status == ActivityStatus.DECLINED_BY_ADMIN

    val canShare: Boolean
        get() = status == ActivityStatus.ON_VOTE || status == ActivityStatus.FINALIZED
}
