package com.dev.lbsuhack.feature.activities.domain

import com.dev.lbsuhack.core.domain.DomainResult
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeActivities(): Flow<List<ActivityProposal>>
    suspend fun refresh(): DomainResult<Unit>
    suspend fun getEventById(id: String): DomainResult<ActivityProposal>
    suspend fun vote(eventId: String): DomainResult<ActivityProposal>
    suspend fun createEvent(
        name: String,
        category: ActivityCategory,
        description: String,
        scope: ActivityScope,
        voteThreshold: Int,
        startsAt: String,
    ): DomainResult<ActivityProposal>
}
