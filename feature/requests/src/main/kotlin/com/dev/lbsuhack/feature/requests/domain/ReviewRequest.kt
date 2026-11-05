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

data class RequestParticipant(
    val id: String,
    val name: String,
    val pupilClass: String,
)

sealed interface ReviewRequest {
    val id: String
    val title: String
    val description: String
    val proposedDate: String
    val author: RequestAuthor
    val status: ReviewStatus
    val audience: RequestAudience
    val declineReason: String?

    data class Publication(
        override val id: String,
        override val title: String,
        override val description: String,
        override val proposedDate: String,
        override val author: RequestAuthor,
        override val status: ReviewStatus,
        override val declineReason: String? = null,
    ) : ReviewRequest {
        override val audience: RequestAudience get() = RequestAudience.TEACHER
    }

    data class Visit(
        override val id: String,
        override val title: String,
        override val description: String,
        override val proposedDate: String,
        val proposedLocation: String,
        override val author: RequestAuthor,
        val participants: List<RequestParticipant>,
        override val status: ReviewStatus,
        val confirmedDate: String? = null,
        val confirmedLocation: String? = null,
        override val declineReason: String? = null,
    ) : ReviewRequest {
        override val audience: RequestAudience get() = RequestAudience.ADMIN
    }
}

val ReviewRequest.isPending: Boolean
    get() = when (audience) {
        RequestAudience.TEACHER -> status == ReviewStatus.PENDING_TEACHER
        RequestAudience.ADMIN -> status == ReviewStatus.PENDING_ADMIN
    }

val ReviewRequest.isProcessed: Boolean
    get() = status == ReviewStatus.APPROVED || status == ReviewStatus.DECLINED
