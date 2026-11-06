package com.dev.lbsuhack.feature.activevote.domain

data class VoteOption(
    val id: String,
    val text: String,
)

data class ActiveVoteEvent(
    val id: String,
    val title: String,
    val category: String,
    val day: String,
    val month: String,
    val time: String,
    val place: String,
    val summary: String,
    val participants: Int,
    val capacity: Int,
)

data class ActiveVoteUser(
    val id: String,
    val name: String,
    val role: String,
    val hasVoted: Boolean,
)

data class ActiveVote(
    val id: String,
    val eventTitle: String,
    val question: String,
    val deadline: String,
    val options: List<VoteOption>,
    val events: List<ActiveVoteEvent> = emptyList(),
    val users: List<ActiveVoteUser> = emptyList(),
)

data class VoteAcceptance(
    val voteId: String,
    val optionId: String,
)
