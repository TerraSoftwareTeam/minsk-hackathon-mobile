package com.dev.lbsuhack.feature.tournaments.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object TournamentsRoute : NavKey

@Serializable
data class TournamentDetailsRoute(
    val tournamentId: String,
) : NavKey
