package com.dev.lbsuhack.feature.requests.data

import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import kotlinx.coroutines.flow.combine
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.DeclineEventDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewRequestRepository
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

class EventReviewRequestRepository(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val observeSession: ObserveSessionUseCase? = null,
) : ReviewRequestRepository {

    private val requestsFlow = MutableStateFlow<List<ReviewRequest>>(emptyList())

    private var owner: String? = sessionId()

    private fun sessionId(): String? =
        (observeSession?.state?.value as? SessionState.Authenticated)?.user?.id

    private fun prepareCache() {
        val userId = sessionId()
        if (owner != userId) {
            requestsFlow.value = emptyList()
            owner = userId
        }
    }

    override fun observeRequests(): Flow<List<ReviewRequest>> = observeSession?.let { session ->
        combine(requestsFlow, session.state) { items, state ->
            val userId = (state as? SessionState.Authenticated)?.user?.id
            if (userId != null && userId == owner) items else emptyList()
        }
    } ?: requestsFlow.asStateFlow()

    override suspend fun refresh(audience: RequestAudience): DomainResult<Unit> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        when (val result = safeCall { httpClient.get(audience.moderationPath()).body<List<EventResponseDto>>() }) {
            is NetworkResult.Success -> {
                if (sessionId() != requestOwner) return@withContext DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Unauthorized)
                requestsFlow.value = result.data.map { it.toDomain(audience) }
                DomainResult.Success(Unit)
            }

            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    override suspend fun approve(
        requestId: String,
        audience: RequestAudience,
    ): DomainResult<ReviewRequest> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
        when (
            val result = safeCall {
                httpClient.post(requestId.actionPath(audience, isDecline = false))
                    .body<EventResponseDto>()
            }
        ) {
            is NetworkResult.Success -> if (sessionId() == requestOwner) {
                DomainResult.Success(updateCache(result.data, audience))
            } else DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Unauthorized)
            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    override suspend fun decline(
        requestId: String,
        reason: String?,
        audience: RequestAudience,
    ): DomainResult<ReviewRequest> = withContext(ioDispatcher) {
        prepareCache()
        val requestOwner = sessionId()
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
            is NetworkResult.Success -> if (sessionId() == requestOwner) {
                DomainResult.Success(updateCache(result.data, audience))
            } else DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Unauthorized)
            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    private fun updateCache(
        event: EventResponseDto,
        audience: RequestAudience,
    ): ReviewRequest {
        val domain = event.toDomain(audience)
        var updated: ReviewRequest? = null
        requestsFlow.update { current ->
            current.map { request ->
                if (request.id == domain.id) {
                    domain.also { updated = it }
                } else {
                    request
                }
            }
        }
        if (updated == null) {
            requestsFlow.update { current -> listOf(domain) + current }
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
        return "api/v1/events/$this/$suffix"
    }
}
