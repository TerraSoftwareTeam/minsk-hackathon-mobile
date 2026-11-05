package com.dev.lbsuhack.feature.feed.domain

enum class EventCategory {
    HACKATHON,
    MEETUP,
    WORKSHOP,
    LECTURE,
}

data class FeedEvent(
    val id: String,
    val title: String,
    val category: EventCategory,
    val day: String,
    val month: String,
    val time: String,
    val place: String,
    val organizer: String,
    val summary: String,
    val description: String,
    val tags: List<String>,
    val participants: Int,
    val capacity: Int,
    val isSubscribed: Boolean = false,
)
