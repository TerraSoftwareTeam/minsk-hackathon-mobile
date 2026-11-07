package com.dev.lbsuhack.feature.requests.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.SessionClearHook
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.NetworkError
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.DeclineEventDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewRequestRepository
import com.dev.lbsuhack.feature.requests.domain.isProcessed
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.withContext

class EventReviewRequestRepository(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val observeSession: ObserveSessionUseCase? = null,
) : ReviewRequestRepository, SessionClearHook {

    private data class AccountCache(val owner: String?, val items: List<ReviewRequest> = emptyList())

    private val requestsFlow = MutableStateFlow(AccountCache(sessionId()))
    private val cacheGeneration = AtomicInteger(0)

    private val operationMutex = Mutex()

    private val owner: String? get() = requestsFlow.value.owner

    override suspend fun beforeClear() {
        cacheGeneration.incrementAndGet()
        requestsFlow.value = AccountCache(null)
    }

    private fun updateRequests(
        requestOwner: String?,
        requestGeneration: Int,
        transform: (List<ReviewRequest>) -> List<ReviewRequest>,
    ) {
        requestsFlow.update { cache ->
            if (cache.owner == requestOwner && cacheGeneration.get() == requestGeneration) {
                cache.copy(items = transform(cache.items))
            } else cache
        }
    }

    private fun sessionId(): String? =
        (observeSession?.state?.value as? SessionState.Authenticated)?.user?.id

    private fun prepareCache() {
        val userId = sessionId()
        if (owner != userId) {
            cacheGeneration.incrementAndGet()
            requestsFlow.value = AccountCache(userId)
        }
    }

    override fun observeRequests(): Flow<List<ReviewRequest>> = observeSession?.let { session ->
        combine(requestsFlow, session.state) { cache, state ->
            val userId = (state as? SessionState.Authenticated)?.user?.id
            if (userId != null && userId == cache.owner) cache.items else emptyList()
        }
    } ?: requestsFlow.map { it.items }

    override suspend fun refresh(audience: RequestAudience): DomainResult<Unit> = withContext(ioDispatcher) {
        operationMutex.withLock {
            prepareCache()
            val requestOwner = sessionId()
            val requestGeneration = cacheGeneration.get()
            when (val result = safeCall { httpClient.get(audience.moderationPath()).body<List<EventResponseDto>>() }) {
                is NetworkResult.Success -> {
                    if (sessionId() != requestOwner || cacheGeneration.get() != requestGeneration) return@withLock DomainResult.Failure(DomainError.Unauthorized)
                    val fresh = result.data.map { it.toDomain(audience) }
                    val freshIds = fresh.mapTo(HashSet()) { it.id }
                    val processed = requestsFlow.value.items
                        .filter { it.isProcessed && it.id !in freshIds }
                        .let { syncProcessed(it, audience) }
                    if (sessionId() != requestOwner || cacheGeneration.get() != requestGeneration) return@withLock DomainResult.Failure(DomainError.Unauthorized)
                    updateRequests(requestOwner, requestGeneration) { fresh + processed }
                    DomainResult.Success(Unit)
                }

                is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
            }
        }
    }

    private suspend fun syncProcessed(
        processed: List<ReviewRequest>,
        audience: RequestAudience,
    ): List<ReviewRequest> = coroutineScope {
        processed.map { request ->
            async {
                when (val result = safeCall { httpClient.get("api/v1/events/${request.id.encodeURLPathPart()}").body<EventResponseDto>() }) {
                    is NetworkResult.Success -> result.data.toDomain(audience).copy(
                        status = request.status,
                        declineReason = request.declineReason,
                    )
                    is NetworkResult.Error -> if (result.error == NetworkError.NotFound) null else request
                }
            }
        }.awaitAll().filterNotNull()
    }

    override suspend fun approve(
        requestId: String,
        audience: RequestAudience,
    ): DomainResult<ReviewRequest> = withContext(ioDispatcher) {
        operationMutex.withLock {
            prepareCache()
            val requestOwner = sessionId()
            val requestGeneration = cacheGeneration.get()
            when (
                val result = safeCall {
                    httpClient.post(requestId.actionPath(audience, isDecline = false)).body<EventResponseDto>()
                }
            ) {
                is NetworkResult.Success -> if (sessionId() == requestOwner && cacheGeneration.get() == requestGeneration) {
                    DomainResult.Success(updateCache(result.data, audience, requestOwner, requestGeneration))
                } else DomainResult.Failure(DomainError.Unauthorized)

                is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
            }
        }
    }

    override suspend fun decline(
        requestId: String,
        reason: String?,
        audience: RequestAudience,
    ): DomainResult<ReviewRequest> = withContext(ioDispatcher) {
        operationMutex.withLock {
            prepareCache()
            val requestOwner = sessionId()
            val requestGeneration = cacheGeneration.get()
            val normalizedReason = reason?.trim()?.ifBlank { null }
            when (
                val result = safeCall {
                    httpClient.post(requestId.actionPath(audience, isDecline = true)) {
                        if (normalizedReason != null) {
                            contentType(ContentType.Application.Json)
                            setBody(DeclineEventDto(reason = normalizedReason))
                        }
                    }.body<EventResponseDto>()
                }
            ) {
                is NetworkResult.Success -> if (sessionId() == requestOwner && cacheGeneration.get() == requestGeneration) {
                    DomainResult.Success(updateCache(result.data, audience, requestOwner, requestGeneration))
                } else DomainResult.Failure(DomainError.Unauthorized)

                is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
            }
        }
    }

    private fun updateCache(
        event: EventResponseDto,
        audience: RequestAudience,
        requestOwner: String?,
        requestGeneration: Int,
    ): ReviewRequest {
        val domain = event.toDomain(audience)
        var updated: ReviewRequest? = null
        updateRequests(requestOwner, requestGeneration) { current ->
            current.map { request ->
                if (request.id == domain.id) {
                    domain.also { updated = it }
                } else {
                    request
                }
            }
        }
        if (updated == null) {
            updateRequests(requestOwner, requestGeneration) { current -> listOf(domain) + current }
        }
        return domain
    }

    private fun RequestAudience.moderationPath(): String = when (this) {
        RequestAudience.TEACHER -> "api/v1/events/moderation"
        RequestAudience.ADMIN -> "api/v1/events/admin-moderation"
    }

    private fun String.actionPath(
        audience: RequestAudience,
        isDecline: Boolean,
    ): String {
        val suffix = when (audience) {
            RequestAudience.TEACHER -> if (isDecline) "decline" else "approve"
            RequestAudience.ADMIN -> if (isDecline) "admin/decline" else "admin/approve"
        }
        return "api/v1/events/${this.encodeURLPathPart()}/$suffix"
    }
}
