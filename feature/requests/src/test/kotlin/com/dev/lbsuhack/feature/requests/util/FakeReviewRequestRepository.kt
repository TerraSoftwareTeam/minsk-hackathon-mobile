package com.dev.lbsuhack.feature.requests.util

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewRequestRepository
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeReviewRequestRepository(
    var teacherQueue: List<ReviewRequest> = emptyList(),
    private val adminQueue: List<ReviewRequest> = emptyList(),
    var refreshResult: DomainResult<Unit> = DomainResult.Success(Unit),
    private val refreshDelayMillis: Long = 0L,
    private val actionDelayMillis: Long = 0L,
) : ReviewRequestRepository {

    var approveCalls = 0
    var approveResult: DomainResult<ReviewRequest>? = null

    private val requestsFlow = MutableStateFlow<List<ReviewRequest>>(emptyList())

    override fun observeRequests(): Flow<List<ReviewRequest>> = requestsFlow.asStateFlow()

    override suspend fun refresh(audience: RequestAudience): DomainResult<Unit> {
        if (refreshDelayMillis > 0L) delay(refreshDelayMillis)
        if (refreshResult is DomainResult.Failure) return refreshResult

        requestsFlow.value = when (audience) {
            RequestAudience.TEACHER -> teacherQueue
            RequestAudience.ADMIN -> adminQueue
        }
        return DomainResult.Success(Unit)
    }

    override suspend fun approve(
        requestId: String,
        audience: RequestAudience,
    ): DomainResult<ReviewRequest> {
        approveCalls++
        if (actionDelayMillis > 0L) delay(actionDelayMillis)
        approveResult?.let { return it }
        return updateRequest(requestId) {
            it.copy(status = ReviewStatus.APPROVED, declineReason = null)
        }
    }

    override suspend fun decline(
        requestId: String,
        reason: String?,
        audience: RequestAudience,
    ): DomainResult<ReviewRequest> {
        if (actionDelayMillis > 0L) delay(actionDelayMillis)
        return updateRequest(requestId) {
            it.copy(status = ReviewStatus.DECLINED, declineReason = reason?.trim()?.ifBlank { null })
        }
    }

    private fun updateRequest(
        requestId: String,
        transform: (ReviewRequest) -> ReviewRequest,
    ): DomainResult<ReviewRequest> {
        val current = requestsFlow.value
        val target = current.find { it.id == requestId } ?: return DomainResult.Failure(DomainError.NotFound)
        val updated = transform(target)
        requestsFlow.value = current.map { if (it.id == requestId) updated else it }
        return DomainResult.Success(updated)
    }
}
