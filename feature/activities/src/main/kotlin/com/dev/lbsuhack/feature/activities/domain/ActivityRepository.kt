package com.dev.lbsuhack.feature.activities.domain

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import kotlinx.coroutines.flow.Flow

data class ActivityMutationFailure(
    val eventId: String,
    val operation: ActivityOperation,
    val error: DomainError,
)

interface ActivityRepository {
    val mutationFailures: Flow<ActivityMutationFailure>

    fun observeActivities(): Flow<List<ActivityProposal>>
    fun cachedEventById(id: String): ActivityProposal?
    suspend fun refresh(): DomainResult<Unit>
    suspend fun getEventById(id: String): DomainResult<ActivityProposal>
    suspend fun uploadPhoto(eventId: String, photoUri: String): DomainResult<ActivityProposal>
    suspend fun vote(eventId: String): DomainResult<ActivityProposal>
    suspend fun unvote(eventId: String): DomainResult<ActivityProposal>
    suspend fun like(eventId: String): DomainResult<ActivityProposal>
    suspend fun dislike(eventId: String): DomainResult<ActivityProposal>
    suspend fun unlike(eventId: String): DomainResult<ActivityProposal>
    suspend fun previewSuccessProbability(
        category: ActivityCategory,
        startsAt: java.time.OffsetDateTime? = null,
    ): DomainResult<Double>
    suspend fun createEvent(
        name: String,
        category: ActivityCategory,
        description: String,
        scope: ActivityScope = ActivityScope.CLASS,
        voteThreshold: Int = 15,
        startsAt: String,
        maxParticipants: Int? = null,
    ): DomainResult<ActivityProposal>
    suspend fun delete(eventId: String): DomainResult<Unit>
}
