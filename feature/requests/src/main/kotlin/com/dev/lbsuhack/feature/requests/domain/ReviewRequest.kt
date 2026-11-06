package com.dev.lbsuhack.feature.requests.domain

enum class RequestAudience {
    TEACHER,
    ADMIN,
}

enum class ReviewStatus {
    PENDING_TEACHER,
    PENDING_ADMIN,
    APPROVED,
    DECLINED,
}

data class RequestAuthor(
    val id: String,
    val name: String,
    val pupilClass: String,
)

data class ReviewRequest(
    val id: String,
    val title: String,
    val description: String,
    val proposedDate: String?,
    val author: RequestAuthor,
    val status: ReviewStatus,
    val audience: RequestAudience,
    val declineReason: String? = null,
    val voters: List<String> = emptyList(),
    val votesCount: Int = 0,
    val voteThreshold: Int = 0,
    val likesCount: Int = 0,
)

val ReviewRequest.isPending: Boolean
    get() = status == ReviewStatus.PENDING_TEACHER || status == ReviewStatus.PENDING_ADMIN

val ReviewRequest.isProcessed: Boolean
    get() = status == ReviewStatus.APPROVED || status == ReviewStatus.DECLINED
