package com.dev.lbsuhack.feature.activities.domain

import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeActivities(): Flow<List<ActivityProposal>>
    suspend fun getActivities(): List<ActivityProposal>
    suspend fun getActivityById(id: String): ActivityProposal?
    suspend fun createProposal(
        title: String,
        category: ActivityCategory,
        description: String,
        targetClass: String,
        minParticipants: Int,
        maxParticipants: Int,
        preferenceNotes: String,
        authorId: String,
        authorName: String,
        authorClass: String,
    ): ActivityProposal
    suspend fun joinActivity(activityId: String, participant: ActivityParticipant): ActivityProposal
    suspend fun leaveActivity(activityId: String, pupilId: String): ActivityProposal
    suspend fun transitionToAdmin(activityId: String): ActivityProposal
    suspend fun approveActivity(
        activityId: String,
        confirmedTime: String,
        confirmedLocation: String,
    ): ActivityProposal
}
