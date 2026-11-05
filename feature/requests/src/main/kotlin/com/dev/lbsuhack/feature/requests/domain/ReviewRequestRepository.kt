package com.dev.lbsuhack.feature.requests.domain

import kotlinx.coroutines.flow.Flow

interface ReviewRequestRepository {

    fun observeRequests(): Flow<List<ReviewRequest>>

    suspend fun approvePublication(requestId: String): ReviewRequest

    suspend fun approveVisit(
        requestId: String,
        confirmedDate: String,
        confirmedLocation: String,
    ): ReviewRequest

    suspend fun decline(
        requestId: String,
        reason: String?,
    ): ReviewRequest
}
