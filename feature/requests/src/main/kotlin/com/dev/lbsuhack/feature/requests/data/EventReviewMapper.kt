package com.dev.lbsuhack.feature.requests.data

import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import java.time.OffsetDateTime

fun EventResponseDto.toDomain(audience: RequestAudience): ReviewRequest {
    val initiator = initiator
    val authorName = buildString {
        append(initiator.name)
        if (initiator.surname.isNotBlank()) append(" ${initiator.surname}")
    }

    return ReviewRequest(
        id = id,
        title = name,
        description = description.orEmpty(),
        proposedDate = startsAt.toOffsetDateTimeOrNull(),
        authorName = authorName,
        voters = voters.map { voter -> listOf(voter.name, voter.surname).filter(String::isNotBlank).joinToString(" ") },
        votesCount = votesCount,
        voteThreshold = voteThreshold,
        likesCount = likesCount,
        status = status.toDomain(),
        audience = audience,
        declineReason = declineReason,
    )
}

fun EventResponseDto.Status.toDomain(): ReviewStatus = when (this) {
    EventResponseDto.Status.ON_MODERATION -> ReviewStatus.PENDING_TEACHER
    EventResponseDto.Status.ON_ADMIN_MODERATION -> ReviewStatus.PENDING_ADMIN
    EventResponseDto.Status.ON_VOTE,
    EventResponseDto.Status.FINALIZED,
    -> ReviewStatus.APPROVED

    EventResponseDto.Status.DECLINED,
    EventResponseDto.Status.DECLINED_BY_ADMIN,
    EventResponseDto.Status.EXPIRED,
    -> ReviewStatus.DECLINED
}

private fun String?.toOffsetDateTimeOrNull(): OffsetDateTime? =
    this?.let { raw -> runCatching { OffsetDateTime.parse(raw) }.getOrNull() }
