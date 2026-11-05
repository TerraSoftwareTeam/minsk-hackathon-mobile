package com.dev.lbsuhack.feature.activities.domain

data class ActivityProposal(
    val id: String,
    val title: String,
    val category: ActivityCategory,
    val description: String,
    val targetClass: String,
    val minParticipants: Int,
    val maxParticipants: Int,
    val authorId: Long,
    val authorName: String,
    val authorClass: String,
    val status: ActivityStatus,
    val participants: List<ActivityParticipant> = emptyList(),
    val preferenceNotes: String = "",
    val confirmedTime: String? = null,
    val confirmedLocation: String? = null,
) {
    val currentParticipantsCount: Int get() = participants.size
    val isFull: Boolean get() = currentParticipantsCount >= maxParticipants
    val hasQuorum: Boolean get() = currentParticipantsCount >= minParticipants

    fun isJoined(pupilId: Long): Boolean = participants.any { it.pupilId == pupilId }
}
