package com.dev.lbsuhack.feature.activities.util

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeActivityRepository(
    initialActivities: List<ActivityProposal> = emptyList(),
) : ActivityRepository {

    var refreshResult: DomainResult<Unit> = DomainResult.Success(Unit)
    var createResult: DomainResult<ActivityProposal>? = null
    private val activities = MutableStateFlow(initialActivities)

    override fun observeActivities(): Flow<List<ActivityProposal>> = activities.asStateFlow()

    var refreshCalls = 0

    override suspend fun refresh(): DomainResult<Unit> {
        refreshCalls++
        return refreshResult
    }

    override suspend fun getEventById(id: String): DomainResult<ActivityProposal> {
        val found = activities.value.find { it.id == id }
        return if (found != null) {
            DomainResult.Success(found)
        } else {
            DomainResult.Failure(DomainError.NotFound)
        }
    }

    var photoResult: DomainResult<ActivityProposal>? = null
    var createCalls = 0
    var photoCalls = 0

    override suspend fun uploadPhoto(eventId: String, photoUri: String): DomainResult<ActivityProposal> {
        photoCalls++
        return photoResult ?: getEventById(eventId)
    }

    override suspend fun vote(eventId: String): DomainResult<ActivityProposal> {
        var updated: ActivityProposal? = null
        activities.update { list ->
            list.map { item ->
                if (item.id == eventId) {
                    val next = item.copy(
                        votesCount = item.votesCount + 1,
                        hasVoted = true,
                    )
                    updated = next
                    next
                } else {
                    item
                }
            }
        }
        return updated?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(DomainError.NotFound)
    }

    override suspend fun createEvent(
        name: String,
        category: ActivityCategory,
        description: String,
        scope: ActivityScope,
        voteThreshold: Int,
    ): DomainResult<ActivityProposal> {
        createCalls++
        createResult?.let { return it }
        val proposal = ActivityProposal(
            id = "created-id",
            title = name,
            description = description,
            category = category,
            scope = scope,
            status = ActivityStatus.ON_MODERATION,
            voteThreshold = voteThreshold,
            votesCount = 0,
            hasVoted = false,
            authorId = "101",
            authorName = "Никита Ковалев",
            authorClass = "10-А",
        )
        activities.update { listOf(proposal) + it }
        return DomainResult.Success(proposal)
    }
}
