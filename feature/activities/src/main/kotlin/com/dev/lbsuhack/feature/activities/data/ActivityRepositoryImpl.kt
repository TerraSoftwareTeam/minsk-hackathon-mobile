package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import kotlinx.coroutines.flow.combine
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.CreateEventDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventVoteResponseDto
import com.dev.lbsuhack.core.network.generated.model.VotingEventResponseDto
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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
    private val observeSession: ObserveSessionUseCase? = null,
    private val imageSource: ActivityImageSource? = null,
) : ActivityRepository {

    private val activitiesFlow = MutableStateFlow<List<ActivityProposal>>(emptyList())

    private var owner: String? = sessionId()

    private fun sessionId(): String? =
        (observeSession?.state?.value as? SessionState.Authenticated)?.user?.id

    private fun prepareCache() {
        val userId = sessionId()
        if (owner != userId) {
            activitiesFlow.value = emptyList()
            owner = userId
        }
    }

    override fun observeActivities(): Flow<List<ActivityProposal>> = observeSession?.let { session ->
        combine(activitiesFlow, session.state) { items, state ->
            val userId = (state as? SessionState.Authenticated)?.user?.id
            if (userId != null && userId == owner) items else emptyList()
        }
    } ?: activitiesFlow.asStateFlow()

    override suspend fun refresh(): DomainResult<Unit> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        val role = (observeSession?.state?.value as? SessionState.Authenticated)?.user?.role
        if (role == UserRole.ORGANIZATOR) {
            val path = "api/v1/events/admin-moderation"
            return@withContext when (val result = safeCall { httpClient.get(path).body<List<EventResponseDto>>() }) {
                is NetworkResult.Success -> {
                    if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
                    activitiesFlow.value = result.data.map { it.toDomain() }
                    DomainResult.Success(Unit)
                }
                is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
            }
        }
        val votingResult = safeCall {
            httpClient.get("api/v1/events/voting").body<List<VotingEventResponseDto>>()
        }

        when (votingResult) {
            is NetworkResult.Success -> {
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
                activitiesFlow.value = votingResult.data.map { it.toDomain() }
                DomainResult.Success(Unit)
            }
            is NetworkResult.Error -> DomainResult.Failure(votingResult.error.toDomain())
        }
    }

    override suspend fun getEventById(id: String): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        if (activitiesFlow.value.none { it.id == id } &&
            (observeSession?.state?.value as? SessionState.Authenticated)?.user?.role in setOf(UserRole.PUPIL, UserRole.TEACHER)) {
            refresh()
        }
        val cached = activitiesFlow.value.find { it.id == id }
        if (cached != null) return@withContext DomainResult.Success(cached)

        when (val result = safeCall { httpClient.get("api/v1/events/$id").body<EventResponseDto>() }) {
            is NetworkResult.Success -> {
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Unauthorized)
                val domain = result.data.toDomain()
                activitiesFlow.update { current -> current.filterNot { it.id == id } + domain }
                DomainResult.Success(domain)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    override suspend fun vote(eventId: String): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        val existing = when (val loaded = getEventById(eventId)) {
            is DomainResult.Success -> loaded.value
            is DomainResult.Failure -> return@withContext loaded
        }
        when (val result = safeCall { httpClient.post("api/v1/events/$eventId/vote").body<EventVoteResponseDto>() }) {
            is NetworkResult.Success -> {
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Unauthorized)
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

                val finalProposal = updatedProposal ?: existing.withVoteResult(voteResponse)
                if (updatedProposal == null) activitiesFlow.update { it + finalProposal }
                DomainResult.Success(finalProposal)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    override suspend fun uploadPhoto(eventId: String, photoUri: String): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        val image = try {
            requireNotNull(imageSource).read(photoUri)
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return@withContext DomainResult.Failure(DomainError.Validation)
        }
        when (val result = safeCall {
            httpClient.put("api/v1/events/$eventId/image") {
                setBody(MultiPartFormDataContent(formData {
                    append("file", image.bytes, Headers.build {
                        append(HttpHeaders.ContentType, image.contentType)
                        append(HttpHeaders.ContentDisposition, "filename=photo")
                    })
                }))
            }.body<EventResponseDto>()
        }) {
            is NetworkResult.Success -> {
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Unauthorized)
                val previous = activitiesFlow.value.find { it.id == eventId }
                val activity = result.data.toDomain(previous?.votesCount ?: 0, previous?.hasVoted ?: false)
                activitiesFlow.update { items -> items.filterNot { it.id == eventId } + activity }
                DomainResult.Success(activity)
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
        prepareCache()
        val requestOwner = sessionId()
        val dto = CreateEventDto(
            name = name.trim(),
            category = category.toDto(),
            voteThreshold = voteThreshold,
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
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Unauthorized)
                val domain = result.data.toDomain()
                activitiesFlow.update { current -> listOf(domain) + current }
                DomainResult.Success(domain)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }
}
