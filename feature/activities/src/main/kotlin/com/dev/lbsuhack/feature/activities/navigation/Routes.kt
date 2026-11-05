package com.dev.lbsuhack.feature.activities.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ActivitiesRoute : NavKey

@Serializable
data object CreateActivityRoute : NavKey

@Serializable
data class ActivityDetailsRoute(
    val activityId: String,
) : NavKey
