package com.dev.lbsuhack.feature.activities.domain

data class ActivityProposal(
    val id: String,
    val title: String,
    val description: String,
    val category: ActivityCategory,
    val scope: ActivityScope = ActivityScope.CLASS,
    val status: ActivityStatus,
    val voteThreshold: Int,
    val maxParticipants: Int? = null,
    val votesCount: Int,
    val hasVoted: Boolean,
    val startsAt: String = "",
    val votingClosesAt: String = "",
    val deadline: String? = null,
    val votingClosesTime: String? = null,
    val deadlineTime: String? = null,
    val createdAt: String = "",
    val authorName: String,
    val teacherName: String? = null,
    val declineReason: String? = null,
    val photoUri: String? = null,
    val confirmedTime: String? = null,
    val voters: List<String> = emptyList(),
    val likesCount: Int = 0,
    val likedByMe: Boolean = false,
    val dislikedByMe: Boolean = false,
    val isMine: Boolean = false,
    val origin: ActivityOrigin = ActivityOrigin.PUPIL,
    val successProbability: Double? = null,
) {
    val showsChance: Boolean
        get() = successProbability != null && (
            status == ActivityStatus.ON_MODERATION ||
                status == ActivityStatus.ON_VOTE ||
                status == ActivityStatus.ON_ADMIN_MODERATION
            )

    val isQuorumReached: Boolean get() = votesCount >= voteThreshold

    val votesTarget: Int
        get() = if (isQuorumReached && maxParticipants != null) maxParticipants else voteThreshold

    val progress: Float
        get() = if (votesTarget > 0) (votesCount.toFloat() / votesTarget).coerceIn(0f, 1f) else 0f

    val isFull: Boolean get() = maxParticipants != null && votesCount >= maxParticipants

    val missedMinimum: Boolean
        get() = status == ActivityStatus.EXPIRED && votesCount < voteThreshold &&
            (origin != ActivityOrigin.PUPIL || votesCount > 1)

    val isPending: Boolean
        get() = status == ActivityStatus.ON_MODERATION ||
            status == ActivityStatus.ON_VOTE ||
            status == ActivityStatus.ON_ADMIN_MODERATION

    val canChangeVote: Boolean get() = status == ActivityStatus.ON_VOTE

    val canWithdrawVote: Boolean get() = canChangeVote && !isMine

    val canLike: Boolean
        get() = status == ActivityStatus.ON_VOTE || status == ActivityStatus.FINALIZED ||
            status == ActivityStatus.DECLINED_BY_ADMIN

    val reactionValue: Int
        get() = when {
            likedByMe -> 1
            dislikedByMe -> -1
            else -> 0
        }

    fun withReaction(value: Int): ActivityProposal {
        require(value in -1..1)
        return copy(
            likesCount = likesCount + value - reactionValue,
            likedByMe = value == 1,
            dislikedByMe = value == -1,
        )
    }

    val canShare: Boolean
        get() = status == ActivityStatus.ON_VOTE || status == ActivityStatus.FINALIZED
}
