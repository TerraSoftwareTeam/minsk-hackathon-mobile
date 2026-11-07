package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.NetworkError
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.CreateEventDto
import com.dev.lbsuhack.core.network.generated.model.EventLikeResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventVoteResponseDto
import com.dev.lbsuhack.core.network.generated.model.VotingEventResponseDto
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityMutationFailure
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class ActivityRepositoryImpl(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val observeSession: ObserveSessionUseCase? = null,
    private val imageSource: ActivityImageSource? = null,
) : ActivityRepository {

    private val activitiesFlow = MutableStateFlow<List<ActivityProposal>>(emptyList())

    private val mutationFailuresFlow = MutableSharedFlow<ActivityMutationFailure>(extraBufferCapacity = 16)

    private val mutationScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private var lastMutationJob: Job? = null

    private var owner: String? = sessionId()

    private val mutations = AtomicInteger(0)

    private val mutationMutex = Mutex()

    override val mutationFailures: Flow<ActivityMutationFailure> = mutationFailuresFlow

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
        val startedAtMutation = mutations.get()
        val role = sessionUser()?.role
        if (role == UserRole.ORGANIZATOR) {
            val result = safeCall { httpClient.get("api/v1/events/admin-moderation").body<List<EventResponseDto>>() }
            return@withContext when (result) {
                is NetworkResult.Success -> {
                    if (sessionId() != requestOwner) return@withContext DomainResult.Failure(DomainError.Unauthorized)
                    mutationMutex.withLock {
                        if (mutations.get() == startedAtMutation) {
                            activitiesFlow.value = result.data.map { it.toDomain() }.newestFirst()
                        }
                    }
                    DomainResult.Success(Unit)
                }
                is NetworkResult.Error -> DomainResult.Failure(result.error.toActivityDomain())
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
        val mine = (myResult as? NetworkResult.Success)?.data.orEmpty().map { it.toDomain(isMine = true) }
        val mineIds = mine.mapTo(HashSet()) { it.id }
        val previousMineIds = activitiesFlow.value.filter { it.isMine }.mapTo(HashSet()) { it.id }
        val voting = (votingResult as? NetworkResult.Success)?.data.orEmpty()
            .filterNot { it.id in mineIds }
            .map { it.toDomain(isMine = myResult is NetworkResult.Error && it.id in previousMineIds) }
        val loaded = mine + voting
        val failure = listOf(votingResult, myResult).filterIsInstance<NetworkResult.Error>().firstOrNull()
        if (failure != null && loaded.isEmpty()) {
            return@withContext DomainResult.Failure(failure.error.toActivityDomain())
        }
        val keptFromFailedSource = if (failure != null) {
            val loadedIds = loaded.mapTo(HashSet()) { it.id }
            activitiesFlow.value.filterNot { it.id in loadedIds }
        } else {
            emptyList()
        }
        mutationMutex.withLock {
            if (mutations.get() == startedAtMutation) {
                activitiesFlow.value = (loaded + keptFromFailedSource).newestFirst()
            }
        }
        if (failure != null) DomainResult.Failure(failure.error.toActivityDomain()) else DomainResult.Success(Unit)
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
                val previous = activitiesFlow.value.find { it.id == id }
                val user = sessionUser()
                val isInitiator = user?.let { u ->
                    result.data.initiator.name.trim().equals(u.name.trim(), ignoreCase = true) &&
                        result.data.initiator.surname.trim().equals(u.surname?.trim(), ignoreCase = true)
                } ?: false
                val isVoter = user?.let { u ->
                    result.data.voters.any {
                        it.name.trim().equals(u.name.trim(), ignoreCase = true) &&
                            it.surname.trim().equals(u.surname?.trim(), ignoreCase = true)
                    }
                } ?: false

                val domain = result.data.toDomain(
                    hasVoted = previous?.hasVoted ?: isVoter,
                    likedByMe = previous?.likedByMe ?: false,
                    isMine = previous?.isMine ?: (if (user != null) isInitiator else (role == UserRole.PUPIL)),
                )
                activitiesFlow.update { current -> (current.filterNot { it.id == id } + domain).newestFirst() }
                DomainResult.Success(domain)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toActivityDomain())
        }
    }

    override suspend fun vote(eventId: String): DomainResult<ActivityProposal> =
        changeVote(eventId, voted = true) { httpClient.post("api/v1/events/$eventId/vote").body() }

    override suspend fun unvote(eventId: String): DomainResult<ActivityProposal> =
        changeVote(eventId, voted = false) { httpClient.delete("api/v1/events/$eventId/vote").body() }

    override suspend fun like(eventId: String): DomainResult<ActivityProposal> =
        changeLike(eventId, liked = true) { httpClient.put("api/v1/events/$eventId/like").body() }

    override suspend fun unlike(eventId: String): DomainResult<ActivityProposal> =
        changeLike(eventId, liked = false) { httpClient.delete("api/v1/events/$eventId/like").body() }

    private suspend fun changeVote(
        eventId: String,
        voted: Boolean,
        request: suspend () -> EventVoteResponseDto,
    ): DomainResult<ActivityProposal> {
        val voterName = sessionUser()?.let { user ->
            listOfNotNull(user.name, user.surname).filter(String::isNotBlank).joinToString(" ")
        }
        return mutate(
            eventId = eventId,
            operation = ActivityOperation.VOTE,
            optimistic = { current ->
                current.copy(
                    votesCount = (current.votesCount + if (voted) 1 else -1).coerceAtLeast(0),
                    hasVoted = voted,
                    voters = when {
                        voterName.isNullOrBlank() -> current.voters
                        voted && voterName !in current.voters -> current.voters + voterName
                        !voted -> current.voters - voterName
                        else -> current.voters
                    },
                )
            },
            request = request,
            applyResult = { proposal, voteResult ->
                proposal.withVoteResult(voteResult, voted = voted, voterName = voterName)
            },
        )
    }

    private suspend fun changeLike(
        eventId: String,
        liked: Boolean,
        request: suspend () -> EventLikeResponseDto,
    ): DomainResult<ActivityProposal> = mutate(
        eventId = eventId,
        operation = ActivityOperation.LIKE,
        optimistic = { current ->
            current.copy(
                likesCount = (current.likesCount + if (liked) 1 else -1).coerceAtLeast(0),
                likedByMe = liked,
            )
        },
        request = request,
        applyResult = { proposal, likeResult -> proposal.withLikeResult(likeResult) },
    )

    private suspend fun <T> mutate(
        eventId: String,
        operation: ActivityOperation,
        optimistic: (ActivityProposal) -> ActivityProposal,
        request: suspend () -> T,
        applyResult: (ActivityProposal, T) -> ActivityProposal,
    ): DomainResult<ActivityProposal> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        val existing = when (val loaded = getEventById(eventId)) {
            is DomainResult.Success -> loaded.value
            is DomainResult.Failure -> return@withContext loaded
        }
        val optimisticState = optimistic(existing)
        mutationMutex.withLock {
            mutations.incrementAndGet()
            activitiesFlow.update { items -> items.replaceOrAdd(optimisticState) }
        }
        val previousJob = lastMutationJob
        lastMutationJob = mutationScope.launch {
            previousJob?.join()
            val result = safeCall { request() }
            mutationMutex.withLock {
                if (sessionId() != requestOwner) return@withLock
                val current = activitiesFlow.value.find { it.id == eventId }
                if (current != optimisticState) return@withLock
                mutations.incrementAndGet()
                when (result) {
                    is NetworkResult.Success -> activitiesFlow.update { items ->
                        items.replaceOrAdd(applyResult(optimisticState, result.data))
                    }

                    is NetworkResult.Error -> {
                        activitiesFlow.update { items -> items.replaceOrAdd(existing) }
                        mutationFailuresFlow.tryEmit(
                            ActivityMutationFailure(eventId, operation, result.error.toActivityDomain()),
                        )
                    }
                }
            }
        }
        DomainResult.Success(optimisticState)
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
                    val activity = mutationMutex.withLock {
                        mutations.incrementAndGet()
                        val previous = activitiesFlow.value.find { it.id == eventId }
                        val activity = result.data.toDomain(
                            hasVoted = previous?.hasVoted ?: false,
                            likedByMe = previous?.likedByMe ?: false,
                            isMine = true,
                        )
                        activitiesFlow.update { items -> items.replaceOrAdd(activity) }
                        activity
                    }
                    DomainResult.Success(activity)
                }
                is NetworkResult.Error -> DomainResult.Failure(result.error.toActivityDomain())
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
                val domain = mutationMutex.withLock {
                    mutations.incrementAndGet()
                    val domain = result.data.toDomain(isMine = true)
                    activitiesFlow.update { items -> items.replaceOrAdd(domain) }
                    domain
                }
                DomainResult.Success(domain)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toActivityDomain())
        }
    }

    private fun List<ActivityProposal>.replaceOrAdd(item: ActivityProposal): List<ActivityProposal> =
        (filterNot { it.id == item.id } + item).newestFirst()

    private fun List<ActivityProposal>.newestFirst(): List<ActivityProposal> =
        sortedByDescending { proposal ->
            runCatching { Instant.parse(proposal.createdAt) }.getOrDefault(Instant.EPOCH)
        }
}

private const val HTTP_FORBIDDEN = 403

private fun NetworkError.toActivityDomain(): DomainError = when {
    this is NetworkError.ClientError && code == HTTP_FORBIDDEN -> DomainError.Forbidden
    else -> toDomain()
}
