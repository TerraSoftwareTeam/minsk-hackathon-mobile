package com.dev.lbsuhack.feature.tournaments.domain

data class Tournament(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val place: String,
    val participants: Int,
    val capacity: Int,
)
