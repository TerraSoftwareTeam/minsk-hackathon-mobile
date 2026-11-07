package com.dev.lbsuhack.feature.activities.util

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityMutationFailure
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeActivityRepository(
    initialActivities: List<ActivityProposal> = emptyList(),
) : ActivityRepository {

    var refreshResult: DomainResult<Unit> = DomainResult.Success(Unit)
    var createResult: DomainResult<ActivityProposal>? = null
    var getEventResult: DomainResult<ActivityProposal>? = null
    var photoResult: DomainResult<ActivityProposal>? = null
    var voteResult: DomainResult<ActivityProposal>? = null
    var unvoteResult: DomainResult<ActivityProposal>? = null
    var likeResult: DomainResult<ActivityProposal>? = null
    var unlikeResult: DomainResult<ActivityProposal>? = null

    var refreshCalls = 0
    var lastStartsAt: String? = null
    var createCalls = 0
    var photoCalls = 0
    var voteCalls = 0
    var unvoteCalls = 0
    var likeCalls = 0
    var dislikeCalls = 0
    var unlikeCalls = 0

    private val activities = MutableStateFlow(initialActivities)

    private val failures = MutableSharedFlow<ActivityMutationFailure>(extraBufferCapacity = 16)

    override val mutationFailures: Flow<ActivityMutationFailure> = failures.asSharedFlow()

    override fun observeActivities(): Flow<List<ActivityProposal>> = activities.asStateFlow()

    var refreshGate: kotlinx.coroutines.CompletableDeferred<Unit>? = null

    override suspend fun refresh(): DomainResult<Unit> {
        refreshCalls++
        refreshGate?.await()
        return refreshResult
    }

    override suspend fun getEventById(id: String): DomainResult<ActivityProposal> {
        getEventResult?.let { return it }
        val found = activities.value.find { it.id == id }
        return if (found != null) {
            DomainResult.Success(found)
        } else {
            DomainResult.Failure(DomainError.NotFound)
        }
    }

    override suspend fun uploadPhoto(eventId: String, photoUri: String): DomainResult<ActivityProposal> {
        photoCalls++
        return photoResult ?: getEventById(eventId)
    }

    override suspend fun vote(eventId: String): DomainResult<ActivityProposal> {
        voteCalls++
        voteResult?.let { return it }
        return change(eventId) { it.copy(votesCount = it.votesCount + 1, hasVoted = true) }
    }

    override suspend fun unvote(eventId: String): DomainResult<ActivityProposal> {
        unvoteCalls++
        unvoteResult?.let { return it }
        return change(eventId) { it.copy(votesCount = (it.votesCount - 1).coerceAtLeast(0), hasVoted = false) }
    }

    override suspend fun like(eventId: String): DomainResult<ActivityProposal> {
        likeCalls++
        likeResult?.let { return it }
        return change(eventId) { it.withReaction(1) }
    }

    override suspend fun dislike(eventId: String): DomainResult<ActivityProposal> {
        dislikeCalls++
        return change(eventId) { it.withReaction(-1) }
    }

    override suspend fun unlike(eventId: String): DomainResult<ActivityProposal> {
        unlikeCalls++
        unlikeResult?.let { return it }
        return change(eventId) { it.withReaction(0) }
    }

    var probabilityResult: DomainResult<Double> = DomainResult.Success(0.58)
    var lastProbabilityRequest: Pair<ActivityCategory, java.time.OffsetDateTime?>? = null

    override suspend fun previewSuccessProbability(
        category: ActivityCategory,
        startsAt: java.time.OffsetDateTime?,
    ): DomainResult<Double> {
        lastProbabilityRequest = category to startsAt
        return probabilityResult
    }

    var lastMaxParticipants: Int? = null
    var deleteResult: DomainResult<Unit> = DomainResult.Success(Unit)
    val deletedIds = mutableListOf<String>()

    override suspend fun delete(eventId: String): DomainResult<Unit> {
        deletedIds += eventId
        if (deleteResult is DomainResult.Success) activities.value = activities.value.filterNot { it.id == eventId }
        return deleteResult
    }

    override suspend fun createEvent(
        name: String,
        category: ActivityCategory,
        description: String,
        scope: ActivityScope,
        voteThreshold: Int,
        startsAt: String,
        maxParticipants: Int?,
    ): DomainResult<ActivityProposal> {
        createCalls++
        lastStartsAt = startsAt
        lastMaxParticipants = maxParticipants
        createResult?.let { return it }
        val proposal = ActivityProposal(
            id = CREATED_ID,
            title = name,
            description = description,
            category = category,
            scope = scope,
            status = ActivityStatus.ON_MODERATION,
            voteThreshold = voteThreshold,
            votesCount = 0,
            hasVoted = false,
            authorName = "Никита Ковалев",
            isMine = true,
        )
        activities.update { listOf(proposal) + it }
        return DomainResult.Success(proposal)
    }

    fun replace(activities: List<ActivityProposal>) {
        this.activities.value = activities
    }

    fun emitMutationFailure(failure: ActivityMutationFailure) {
        failures.tryEmit(failure)
    }

    private fun change(eventId: String, transform: (ActivityProposal) -> ActivityProposal): DomainResult<ActivityProposal> {
        var updated: ActivityProposal? = null
        activities.update { list ->
            list.map { item -> if (item.id == eventId) transform(item).also { updated = it } else item }
        }
        return updated?.let { DomainResult.Success(it) } ?: DomainResult.Failure(DomainError.NotFound)
    }

    companion object {
        const val CREATED_ID = "created-id"
    }
}
