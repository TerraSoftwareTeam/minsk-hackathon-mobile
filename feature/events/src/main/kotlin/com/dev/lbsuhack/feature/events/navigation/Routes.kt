package com.dev.lbsuhack.feature.events.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object EventsRoute : NavKey

@Serializable
data class EventDetailsRoute(
    val eventId: String,
) : NavKey
