package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.core.network.generated.model.CreateEventDto
import com.dev.lbsuhack.core.network.generated.model.EventLikeResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventUserResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventVoteResponseDto
import com.dev.lbsuhack.core.network.generated.model.VotingEventResponseDto
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun VotingEventResponseDto.toDomain(isMine: Boolean = false): ActivityProposal = ActivityProposal(
    id = id,
    title = name,
    description = description.orEmpty(),
    category = category.toDomain(),
    scope = scope.toDomain(),
    status = status.toDomain(),
    voteThreshold = voteThreshold,
    votesCount = votesCount,
    hasVoted = hasVoted,
    startsAt = startsAt.orEmpty(),
    votingClosesAt = votingClosesAt,
    createdAt = createdAt,
    authorName = initiator.fullName(),
    teacherName = teacher?.fullName(),
    declineReason = declineReason,
    photoUri = imageUrl,
    confirmedTime = startsAt?.toDisplayDateTime(),
    voters = voters.map { it.fullName() },
    likesCount = likesCount,
    likedByMe = likedByMe,
    isMine = isMine,
)

fun EventResponseDto.toDomain(
    hasVoted: Boolean = false,
    likedByMe: Boolean = false,
    isMine: Boolean = false,
): ActivityProposal = ActivityProposal(
    id = id,
    title = name,
    description = description.orEmpty(),
    category = category.toDomain(),
    scope = scope.toDomain(),
    status = status.toDomain(),
    voteThreshold = voteThreshold,
    votesCount = votesCount,
    hasVoted = hasVoted,
    startsAt = startsAt.orEmpty(),
    votingClosesAt = votingClosesAt,
    createdAt = createdAt,
    authorName = initiator.fullName(),
    teacherName = teacher?.fullName(),
    declineReason = declineReason,
    photoUri = imageUrl,
    confirmedTime = startsAt?.toDisplayDateTime(),
    voters = voters.map { it.fullName() },
    likesCount = likesCount,
    likedByMe = likedByMe,
    isMine = isMine,
)

private fun EventUserResponseDto.fullName(): String =
    listOf(name, surname).filter(String::isNotBlank).joinToString(" ")

fun ActivityProposal.withVoteResult(
    voteResult: EventVoteResponseDto,
    voted: Boolean,
    voterName: String?,
): ActivityProposal = copy(
    votesCount = voteResult.votesCount,
    voteThreshold = voteResult.voteThreshold,
    status = voteResult.status.toDomain(),
    hasVoted = voted,
    voters = when {
        voterName.isNullOrBlank() -> voters
        voted && voterName !in voters -> voters + voterName
        !voted -> voters.toMutableList().apply { remove(voterName) }
        else -> voters
    },
)

fun ActivityProposal.withLikeResult(likeResult: EventLikeResponseDto): ActivityProposal = copy(
    likesCount = likeResult.likesCount,
    likedByMe = likeResult.liked,
)

fun ActivityCategory.toDto(): CreateEventDto.Category = when (this) {
    ActivityCategory.SPORTS -> CreateEventDto.Category.SPORTS
    ActivityCategory.INTELLECTUAL -> CreateEventDto.Category.INTELLECTUAL
    ActivityCategory.CULTURAL -> CreateEventDto.Category.CULTURAL
    ActivityCategory.CREATIVE -> CreateEventDto.Category.CREATIVE
    ActivityCategory.EDUCATIONAL -> CreateEventDto.Category.EDUCATIONAL
    ActivityCategory.VOLUNTEER -> CreateEventDto.Category.VOLUNTEER
    ActivityCategory.ENVIRONMENTAL -> CreateEventDto.Category.ENVIRONMENTAL
    ActivityCategory.TOURISM -> CreateEventDto.Category.TOURISM
    ActivityCategory.ENTERTAINMENT -> CreateEventDto.Category.ENTERTAINMENT
    ActivityCategory.OTHER -> CreateEventDto.Category.OTHER
}

fun VotingEventResponseDto.Category.toDomain(): ActivityCategory = when (this) {
    VotingEventResponseDto.Category.SPORTS -> ActivityCategory.SPORTS
    VotingEventResponseDto.Category.INTELLECTUAL -> ActivityCategory.INTELLECTUAL
    VotingEventResponseDto.Category.CULTURAL -> ActivityCategory.CULTURAL
    VotingEventResponseDto.Category.CREATIVE -> ActivityCategory.CREATIVE
    VotingEventResponseDto.Category.EDUCATIONAL -> ActivityCategory.EDUCATIONAL
    VotingEventResponseDto.Category.VOLUNTEER -> ActivityCategory.VOLUNTEER
    VotingEventResponseDto.Category.ENVIRONMENTAL -> ActivityCategory.ENVIRONMENTAL
    VotingEventResponseDto.Category.TOURISM -> ActivityCategory.TOURISM
    VotingEventResponseDto.Category.ENTERTAINMENT -> ActivityCategory.ENTERTAINMENT
    VotingEventResponseDto.Category.OTHER -> ActivityCategory.OTHER
}

fun EventResponseDto.Category.toDomain(): ActivityCategory = when (this) {
    EventResponseDto.Category.SPORTS -> ActivityCategory.SPORTS
    EventResponseDto.Category.INTELLECTUAL -> ActivityCategory.INTELLECTUAL
    EventResponseDto.Category.CULTURAL -> ActivityCategory.CULTURAL
    EventResponseDto.Category.CREATIVE -> ActivityCategory.CREATIVE
    EventResponseDto.Category.EDUCATIONAL -> ActivityCategory.EDUCATIONAL
    EventResponseDto.Category.VOLUNTEER -> ActivityCategory.VOLUNTEER
    EventResponseDto.Category.ENVIRONMENTAL -> ActivityCategory.ENVIRONMENTAL
    EventResponseDto.Category.TOURISM -> ActivityCategory.TOURISM
    EventResponseDto.Category.ENTERTAINMENT -> ActivityCategory.ENTERTAINMENT
    EventResponseDto.Category.OTHER -> ActivityCategory.OTHER
}

fun ActivityScope.toDto(): CreateEventDto.Scope = when (this) {
    ActivityScope.CLASS -> CreateEventDto.Scope.CLASS
    ActivityScope.GRADE -> CreateEventDto.Scope.GRADE
    ActivityScope.SCHOOL -> CreateEventDto.Scope.SCHOOL
}

fun VotingEventResponseDto.Scope.toDomain(): ActivityScope = when (this) {
    VotingEventResponseDto.Scope.CLASS -> ActivityScope.CLASS
    VotingEventResponseDto.Scope.GRADE -> ActivityScope.GRADE
    VotingEventResponseDto.Scope.SCHOOL -> ActivityScope.SCHOOL
}

fun EventResponseDto.Scope.toDomain(): ActivityScope = when (this) {
    EventResponseDto.Scope.CLASS -> ActivityScope.CLASS
    EventResponseDto.Scope.GRADE -> ActivityScope.GRADE
    EventResponseDto.Scope.SCHOOL -> ActivityScope.SCHOOL
}

fun VotingEventResponseDto.Status.toDomain(): ActivityStatus = when (this) {
    VotingEventResponseDto.Status.ON_MODERATION -> ActivityStatus.ON_MODERATION
    VotingEventResponseDto.Status.DECLINED -> ActivityStatus.DECLINED
    VotingEventResponseDto.Status.ON_VOTE -> ActivityStatus.ON_VOTE
    VotingEventResponseDto.Status.EXPIRED -> ActivityStatus.EXPIRED
    VotingEventResponseDto.Status.ON_ADMIN_MODERATION -> ActivityStatus.ON_ADMIN_MODERATION
    VotingEventResponseDto.Status.FINALIZED -> ActivityStatus.FINALIZED
    VotingEventResponseDto.Status.DECLINED_BY_ADMIN -> ActivityStatus.DECLINED_BY_ADMIN
}

fun EventResponseDto.Status.toDomain(): ActivityStatus = when (this) {
    EventResponseDto.Status.ON_MODERATION -> ActivityStatus.ON_MODERATION
    EventResponseDto.Status.DECLINED -> ActivityStatus.DECLINED
    EventResponseDto.Status.ON_VOTE -> ActivityStatus.ON_VOTE
    EventResponseDto.Status.EXPIRED -> ActivityStatus.EXPIRED
    EventResponseDto.Status.ON_ADMIN_MODERATION -> ActivityStatus.ON_ADMIN_MODERATION
    EventResponseDto.Status.FINALIZED -> ActivityStatus.FINALIZED
    EventResponseDto.Status.DECLINED_BY_ADMIN -> ActivityStatus.DECLINED_BY_ADMIN
}

fun EventVoteResponseDto.Status.toDomain(): ActivityStatus = when (this) {
    EventVoteResponseDto.Status.ON_MODERATION -> ActivityStatus.ON_MODERATION
    EventVoteResponseDto.Status.DECLINED -> ActivityStatus.DECLINED
    EventVoteResponseDto.Status.ON_VOTE -> ActivityStatus.ON_VOTE
    EventVoteResponseDto.Status.EXPIRED -> ActivityStatus.EXPIRED
    EventVoteResponseDto.Status.ON_ADMIN_MODERATION -> ActivityStatus.ON_ADMIN_MODERATION
    EventVoteResponseDto.Status.FINALIZED -> ActivityStatus.FINALIZED
    EventVoteResponseDto.Status.DECLINED_BY_ADMIN -> ActivityStatus.DECLINED_BY_ADMIN
}

internal fun String.toDisplayDateTime(zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
    runCatching {
        OffsetDateTime.parse(this)
            .atZoneSameInstant(zone)
            .format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", locale))
    }.getOrDefault(this)
