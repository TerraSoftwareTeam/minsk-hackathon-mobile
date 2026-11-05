package com.dev.lbsuhack.feature.tournaments.store.tournaments

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.tournaments.domain.TournamentBracket
import com.dev.lbsuhack.feature.tournaments.domain.TournamentMatch
import com.dev.lbsuhack.feature.tournaments.domain.TournamentStage

interface TournamentsStore : Store<TournamentsStore.Intent, TournamentsStore.State, TournamentsStore.Label> {

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data class SelectStage(val stage: TournamentStage) : Intent
        data class SelectMatch(val matchId: String?) : Intent
        data object ResetViewport : Intent
    }

    data class State(
        val stage: TournamentStage = TournamentStage.SCHOOL,
        val bracket: TournamentBracket? = null,
        val selectedMatch: TournamentMatch? = null,
        val resetViewportTrigger: Long = 0L,
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label
}
