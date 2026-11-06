package com.dev.lbsuhack.feature.createvote.domain

data class VoteParticipant(
    val id: String,
    val name: String,
    val pupilClass: String,
)

data class VoteEvent(
    val id: String,
    val title: String,
    val date: String,
    val participants: List<VoteParticipant>,
)
