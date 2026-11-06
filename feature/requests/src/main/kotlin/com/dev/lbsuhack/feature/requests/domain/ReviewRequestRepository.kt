package com.dev.lbsuhack.feature.requests.domain

import com.dev.lbsuhack.core.domain.DomainResult
import kotlinx.coroutines.flow.Flow

interface ReviewRequestRepository {

    fun observeRequests(): Flow<List<ReviewRequest>>

    suspend fun refresh(audience: RequestAudience): DomainResult<Unit>

    suspend fun approve(
        requestId: String,
        audience: RequestAudience,
        startsAt: String? = null,
    ): DomainResult<ReviewRequest>

    suspend fun decline(
        requestId: String,
        reason: String?,
        audience: RequestAudience,
    ): DomainResult<ReviewRequest>
}
