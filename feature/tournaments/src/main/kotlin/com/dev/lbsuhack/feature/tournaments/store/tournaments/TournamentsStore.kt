package com.dev.lbsuhack.feature.tournaments.store.tournaments

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.tournaments.domain.Tournament

interface TournamentsStore : Store<TournamentsStore.Intent, TournamentsStore.State, TournamentsStore.Label> {

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data class ClickTournament(val tournamentId: String) : Intent
    }

    data class State(
        val tournaments: List<Tournament> = emptyList(),
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label {
        data class OpenDetails(val tournamentId: String) : Label
    }
}
