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
    val authorId: String,
    val authorName: String,
    val authorClass: String,
    val authorSchoolsById: String? = null,
    val teacherName: String? = null,
    val declineReason: String? = null,
    val photoUri: String? = null,
    val initiative: ActivityInitiative = ActivityInitiative.STUDENT,
    val targetClass: String = authorClass,
    val minParticipants: Int = voteThreshold,
    val maxParticipants: Int = voteThreshold,
    val preferenceNotes: String = "",
    val confirmedTime: String? = null,
    val confirmedLocation: String? = null,
    val voters: List<String> = emptyList(),
    val likesCount: Int = 0,
    val likedByMe: Boolean = false,
    val isMine: Boolean = false,
) {
    val currentParticipantsCount: Int get() = votesCount
    val isFull: Boolean get() = maxParticipants > 0 && currentParticipantsCount >= maxParticipants
    val hasQuorum: Boolean get() = isQuorumReached
    val isQuorumReached: Boolean get() = votesCount >= voteThreshold
    val progress: Float
        get() = if (voteThreshold > 0) (votesCount.toFloat() / voteThreshold).coerceIn(0f, 1f) else 0f

    fun isCreatedBy(schoolsById: String?): Boolean =
        !schoolsById.isNullOrBlank() && schoolsById == authorSchoolsById

    fun isCreatedBy(userId: String?, schoolsById: String?): Boolean =
        isMine || (!userId.isNullOrBlank() && userId == authorId) || isCreatedBy(schoolsById)

    val canChangeVote: Boolean get() = status == ActivityStatus.ON_VOTE

    val canLike: Boolean
        get() = status == ActivityStatus.ON_VOTE || status == ActivityStatus.ON_ADMIN_MODERATION ||
            status == ActivityStatus.FINALIZED || status == ActivityStatus.DECLINED_BY_ADMIN
}
