package com.dev.lbsuhack.feature.createvote.domain

data class VoteDraft(
    val eventId: String,
    val title: String,
    val description: String,
    val deadline: String,
    val participantIds: Set<String>,
)

data class CreatedVote(
    val id: String,
    val eventId: String,
    val title: String,
    val description: String,
    val deadline: String,
    val participantIds: Set<String>,
)
