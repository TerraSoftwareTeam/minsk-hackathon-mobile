package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.core.network.generated.model.CreateEventDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventVoteResponseDto
import com.dev.lbsuhack.core.network.generated.model.VotingEventResponseDto
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

fun VotingEventResponseDto.toDomain(): ActivityProposal {
    val author = initiator
    val authorFullName = buildString {
        append(author.name)
        if (author.surname.isNotBlank()) append(" ${author.surname}")
    }
    val teacherFullName = buildString {
        append(teacher.name)
        if (teacher.surname.isNotBlank()) append(" ${teacher.surname}")
    }

    return ActivityProposal(
        id = id,
        title = name,
        description = description.orEmpty(),
        category = category.toDomain(),
        scope = scope.toDomain(),
        status = status.toDomain(),
        voteThreshold = voteThreshold.toInt(),
        votesCount = votesCount.toInt(),
        hasVoted = hasVoted,
        startsAt = startsAt,
        votingClosesAt = votingClosesAt,
        authorId = author.id,
        authorName = authorFullName,
        authorClass = author.propertyClass.orEmpty(),
        teacherName = teacherFullName,
        declineReason = declineReason,
    )
}

fun EventResponseDto.toDomain(votesCount: Int = 0, hasVoted: Boolean = false): ActivityProposal {
    val author = initiator
    val authorFullName = buildString {
        append(author.name)
        if (author.surname.isNotBlank()) append(" ${author.surname}")
    }
    val teacherFullName = buildString {
        append(teacher.name)
        if (teacher.surname.isNotBlank()) append(" ${teacher.surname}")
    }

    return ActivityProposal(
        id = id,
        title = name,
        description = description.orEmpty(),
        category = category.toDomain(),
        scope = scope.toDomain(),
        status = status.toDomain(),
        voteThreshold = voteThreshold.toInt(),
        votesCount = votesCount,
        hasVoted = hasVoted,
        startsAt = startsAt,
        votingClosesAt = votingClosesAt,
        authorId = author.id,
        authorName = authorFullName,
        authorClass = author.propertyClass.orEmpty(),
        teacherName = teacherFullName,
        declineReason = declineReason,
    )
}

fun ActivityProposal.withVoteResult(voteResult: EventVoteResponseDto): ActivityProposal {
    return copy(
        votesCount = voteResult.votesCount.toInt(),
        voteThreshold = voteResult.voteThreshold.toInt(),
        status = voteResult.status.toDomain(),
        hasVoted = true,
    )
}

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
