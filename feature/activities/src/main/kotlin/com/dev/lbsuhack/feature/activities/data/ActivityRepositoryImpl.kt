package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.CreateEventDto
import com.dev.lbsuhack.core.network.generated.model.EventLikeResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventVoteResponseDto
import com.dev.lbsuhack.core.network.generated.model.VotingEventResponseDto
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.time.Instant

class ActivityRepositoryImpl(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val observeSession: ObserveSessionUseCase? = null,
    private val imageSource: ActivityImageSource? = null,
) : ActivityRepository {

    private val activitiesFlow = MutableStateFlow<List<ActivityProposal>>(emptyList())

    private var owner: String? = sessionId()

    @Volatile
    private var mutations = 0

    private fun sessionUser(): SessionUser? =
        (observeSession?.state?.value as? SessionState.Authenticated)?.user

    private fun sessionId(): String? = sessionUser()?.id

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
        val startedAtMutation = mutations
        val role = sessionUser()?.role
        if (role == UserRole.ORGANIZATOR) {
            val result = safeCall { httpClient.get("api/v1/events/admin-moderation").body<List<EventResponseDto>>() }
            return@withContext when (result) {
                is NetworkResult.Success -> {
                    if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
                    if (mutations == startedAtMutation) {
                        activitiesFlow.value = result.data.map { it.toDomain() }.newestFirst()
                    }
                    DomainResult.Success(Unit)
                }
                is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
            }
        }
        val (votingResult, myResult) = coroutineScope {
            val voting = async {
                safeCall { httpClient.get("api/v1/events/voting").body<List<VotingEventResponseDto>>() }
            }
            val mine = async {
                if (role == UserRole.PUPIL) {
                    safeCall { httpClient.get("api/v1/events/my").body<List<VotingEventResponseDto>>() }
                } else {
                    NetworkResult.Success(emptyList())
                }
            }
            voting.await() to mine.await()
        }

        if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
        if (mutations != startedAtMutation) return@withContext DomainResult.Success(Unit)
        val mine = (myResult as? NetworkResult.Success)?.data.orEmpty().map { it.toDomain(isMine = true) }
        val mineIds = mine.mapTo(HashSet()) { it.id }
        val previousMineIds = activitiesFlow.value.filter { it.isMine }.mapTo(HashSet()) { it.id }
        val voting = (votingResult as? NetworkResult.Success)?.data.orEmpty()
            .filterNot { it.id in mineIds }
            .map { it.toDomain(isMine = myResult is NetworkResult.Error && it.id in previousMineIds) }
        val loaded = mine + voting
        val failure = listOf(votingResult, myResult).filterIsInstance<NetworkResult.Error>().firstOrNull()
        if (failure != null && loaded.isEmpty()) {
            return@withContext DomainResult.Failure(failure.error.toDomain())
        }
        val keptFromFailedSource = if (failure != null) {
            val loadedIds = loaded.mapTo(HashSet()) { it.id }
            activitiesFlow.value.filterNot { it.id in loadedIds }
        } else {
            emptyList()
        }
        activitiesFlow.value = (loaded + keptFromFailedSource).newestFirst()
        if (failure != null) DomainResult.Failure(failure.error.toDomain()) else DomainResult.Success(Unit)
    }

    override suspend fun getEventById(id: String): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        val role = sessionUser()?.role
        if (activitiesFlow.value.none { it.id == id } && role == UserRole.PUPIL) {
            refresh()
        }
        activitiesFlow.value.find { it.id == id }?.let { return@withContext DomainResult.Success(it) }

        when (val result = safeCall { httpClient.get("api/v1/events/$id").body<EventResponseDto>() }) {
            is NetworkResult.Success -> {
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
                val domain = result.data.toDomain(isMine = role == UserRole.PUPIL)
                activitiesFlow.update { current -> (current.filterNot { it.id == id } + domain).newestFirst() }
                DomainResult.Success(domain)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    override suspend fun vote(eventId: String): DomainResult<ActivityProposal> =
        changeVote(eventId, voted = true) { httpClient.post("api/v1/events/$eventId/vote").body() }

    override suspend fun unvote(eventId: String): DomainResult<ActivityProposal> =
        changeVote(eventId, voted = false) { httpClient.delete("api/v1/events/$eventId/vote").body() }

    override suspend fun like(eventId: String): DomainResult<ActivityProposal> =
        changeLike(eventId) { httpClient.put("api/v1/events/$eventId/like").body() }

    override suspend fun unlike(eventId: String): DomainResult<ActivityProposal> =
        changeLike(eventId) { httpClient.delete("api/v1/events/$eventId/like").body() }

    private suspend fun changeVote(
        eventId: String,
        voted: Boolean,
        request: suspend () -> EventVoteResponseDto,
    ): DomainResult<ActivityProposal> {
        val voterName = sessionUser()?.let { user ->
            listOfNotNull(user.name, user.surname).filter(String::isNotBlank).joinToString(" ")
        }
        return mutate(eventId, request) { withVoteResult(it, voted = voted, voterName = voterName) }
    }

    private suspend fun changeLike(
        eventId: String,
        request: suspend () -> EventLikeResponseDto,
    ): DomainResult<ActivityProposal> = mutate(eventId, request) { withLikeResult(it) }

    private suspend fun <T> mutate(
        eventId: String,
        request: suspend () -> T,
        apply: ActivityProposal.(T) -> ActivityProposal,
    ): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        val existing = when (val loaded = getEventById(eventId)) {
            is DomainResult.Success -> loaded.value
            is DomainResult.Failure -> return@withContext loaded
        }
        when (val result = safeCall { request() }) {
            is NetworkResult.Success -> {
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
                mutations++
                val current = activitiesFlow.value.find { it.id == eventId } ?: existing
                val updated = current.apply(result.data)
                activitiesFlow.update { items -> items.replaceOrAdd(updated) }
                DomainResult.Success(updated)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    override suspend fun uploadPhoto(eventId: String, photoUri: String): DomainResult<ActivityProposal> =
        withContext(ioDispatcher) {
            prepareCache()
            val requestOwner = sessionId()
            val image = try {
                requireNotNull(imageSource).read(photoUri)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                return@withContext DomainResult.Failure(DomainError.Validation)
            }
            val result = safeCall {
                httpClient.put("api/v1/events/$eventId/image") {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(
                                    "file",
                                    image.bytes,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, image.contentType)
                                        append(HttpHeaders.ContentDisposition, "filename=photo")
                                    },
                                )
                            },
                        ),
                    )
                }.body<EventResponseDto>()
            }
            when (result) {
                is NetworkResult.Success -> {
                    if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
                    mutations++
                    val previous = activitiesFlow.value.find { it.id == eventId }
                    val activity = result.data.toDomain(
                        hasVoted = previous?.hasVoted ?: false,
                        likedByMe = previous?.likedByMe ?: false,
                        isMine = true,
                    )
                    activitiesFlow.update { items -> items.replaceOrAdd(activity) }
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
    ): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        val dto = CreateEventDto(
            name = name.trim(),
            category = category.toDto(),
            voteThreshold = voteThreshold,
            description = description.trim().ifEmpty { null },
            scope = scope.toDto(),
        )
        val result = safeCall {
            httpClient.post("api/v1/events") {
                contentType(ContentType.Application.Json)
                setBody(dto)
            }.body<EventResponseDto>()
        }
        when (result) {
            is NetworkResult.Success -> {
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
                mutations++
                val domain = result.data.toDomain(isMine = true)
                activitiesFlow.update { items -> items.replaceOrAdd(domain) }
                DomainResult.Success(domain)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    private fun List<ActivityProposal>.replaceOrAdd(item: ActivityProposal): List<ActivityProposal> =
        (filterNot { it.id == item.id } + item).newestFirst()

    private fun List<ActivityProposal>.newestFirst(): List<ActivityProposal> =
        sortedByDescending { proposal ->
            runCatching { Instant.parse(proposal.createdAt) }.getOrDefault(Instant.EPOCH)
        }
}
