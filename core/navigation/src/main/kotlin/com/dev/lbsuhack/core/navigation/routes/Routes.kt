package com.dev.lbsuhack.core.navigation.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


@Serializable
data object HomeRoute : NavKey

@Serializable
data class DetailsRoute(
    val itemId: String,
    val title: String = "",
) : NavKey

@Serializable
data object ProfileRoute : NavKey
