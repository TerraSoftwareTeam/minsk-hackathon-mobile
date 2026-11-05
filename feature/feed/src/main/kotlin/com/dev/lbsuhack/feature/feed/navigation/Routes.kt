package com.dev.lbsuhack.feature.feed.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object FeedRoute : NavKey

@Serializable
data class FeedDetailsRoute(
    val eventId: String,
) : NavKey
