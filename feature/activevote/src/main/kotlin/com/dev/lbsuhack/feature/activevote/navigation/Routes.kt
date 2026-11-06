package com.dev.lbsuhack.feature.activevote.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ActiveVoteRoute(
    val voteId: String,
) : NavKey
