package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.CreateEventDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventVoteResponseDto
import com.dev.lbsuhack.core.network.generated.model.VotingEventResponseDto
import java.math.BigDecimal
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ActivityRepositoryImpl(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ActivityRepository {

    private val activitiesFlow = MutableStateFlow<List<ActivityProposal>>(emptyList())

    override fun observeActivities(): Flow<List<ActivityProposal>> = activitiesFlow.asStateFlow()

    override suspend fun refresh(): DomainResult<Unit> = withContext(ioDispatcher) {
        val votingResult = safeCall {
            httpClient.get("api/v1/events/voting").body<List<VotingEventResponseDto>>()
        }

        val myResult = safeCall {
            httpClient.get("api/v1/events/my").body<List<VotingEventResponseDto>>()
        }

        when {
            votingResult is NetworkResult.Success -> {
                val votingProposals = votingResult.data.map { it.toDomain() }
                val myProposals = if (myResult is NetworkResult.Success) {
                    myResult.data.map { it.toDomain() }
                } else {
                    emptyList()
                }

                val merged = (votingProposals + myProposals).distinctBy { it.id }
                activitiesFlow.value = merged
                DomainResult.Success(Unit)
            }

            votingResult is NetworkResult.Error -> {
                DomainResult.Failure(votingResult.error.toDomain())
            }

            else -> DomainResult.Failure(DomainError.Unknown)
        }
    }

    override suspend fun getEventById(id: String): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        val cached = activitiesFlow.value.find { it.id == id }
        if (cached != null) return@withContext DomainResult.Success(cached)

        when (val result = safeCall { httpClient.get("api/v1/events/$id").body<EventResponseDto>() }) {
            is NetworkResult.Success -> {
                val domain = result.data.toDomain()
                DomainResult.Success(domain)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    override suspend fun vote(eventId: String): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        when (val result = safeCall { httpClient.post("api/v1/events/$eventId/vote").body<EventVoteResponseDto>() }) {
            is NetworkResult.Success -> {
                val voteResponse = result.data
                var updatedProposal: ActivityProposal? = null
                activitiesFlow.update { current ->
                    current.map { item ->
                        if (item.id == eventId) {
                            item.withVoteResult(voteResponse).also { updatedProposal = it }
                        } else {
                            item
                        }
                    }
                }

                val finalProposal = updatedProposal ?: ActivityProposal(
                    id = voteResponse.eventId,
                    title = "",
                    description = "",
                    category = ActivityCategory.OTHER,
                    scope = ActivityScope.CLASS,
                    status = voteResponse.status.toDomain(),
                    voteThreshold = voteResponse.voteThreshold.toInt(),
                    votesCount = voteResponse.votesCount.toInt(),
                    hasVoted = true,
                    startsAt = "",
                    votingClosesAt = "",
                    authorId = "",
                    authorName = "",
                    authorClass = "",
                )
                DomainResult.Success(finalProposal)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    override suspend fun createEvent(
        name: String,
        category: ActivityCategory,
        description: String,
        scope: ActivityScope,
        voteThreshold: Int,
        startsAt: String,
    ): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        val dto = CreateEventDto(
            name = name.trim(),
            category = category.toDto(),
            voteThreshold = BigDecimal.valueOf(voteThreshold.toLong()),
            startsAt = startsAt,
            description = description.trim().ifEmpty { null },
            scope = scope.toDto(),
        )

        when (
            val result = safeCall {
                httpClient.post("api/v1/events") {
                    contentType(ContentType.Application.Json)
                    setBody(dto)
                }.body<EventResponseDto>()
            }
        ) {
            is NetworkResult.Success -> {
                val domain = result.data.toDomain()
                activitiesFlow.update { current -> listOf(domain) + current }
                DomainResult.Success(domain)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }
}
