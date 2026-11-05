package com.dev.lbsuhack.feature.tournaments.store.details

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.tournaments.domain.Tournament

interface TournamentDetailsStore : Store<TournamentDetailsStore.Intent, TournamentDetailsStore.State, Nothing> {

    sealed interface Intent {
        data class Load(val tournamentId: String) : Intent
        data object Retry : Intent
    }

    data class State(
        val tournamentId: String = "",
        val tournament: Tournament? = null,
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )
}
