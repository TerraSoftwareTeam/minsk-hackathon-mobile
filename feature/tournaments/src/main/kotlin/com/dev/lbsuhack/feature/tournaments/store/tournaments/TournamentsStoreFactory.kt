package com.dev.lbsuhack.feature.tournaments.store.tournaments

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.tournaments.domain.TournamentBracket
import com.dev.lbsuhack.feature.tournaments.domain.TournamentMatch
import com.dev.lbsuhack.feature.tournaments.domain.TournamentRepository
import com.dev.lbsuhack.feature.tournaments.domain.TournamentStage
import com.dev.lbsuhack.feature.tournaments.toUiText
import kotlinx.coroutines.launch

class TournamentsStoreFactory(
    private val storeFactory: StoreFactory,
    private val tournamentRepository: TournamentRepository,
) {

    fun create(): TournamentsStore =
        object : TournamentsStore,
            Store<TournamentsStore.Intent, TournamentsStore.State, TournamentsStore.Label> by storeFactory.create(
                name = "TournamentsStore",
                initialState = TournamentsStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data object Loading : Message
        data class Loaded(val bracket: TournamentBracket) : Message
        data class Failed(val error: UiText) : Message
        data class StageSelected(val stage: TournamentStage) : Message
        data class MatchSelected(val match: TournamentMatch?) : Message
        data object ViewportReset : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<TournamentsStore.Intent, Nothing, TournamentsStore.State, Message, TournamentsStore.Label>() {

        override fun executeIntent(intent: TournamentsStore.Intent) {
            when (intent) {
                TournamentsStore.Intent.Load, TournamentsStore.Intent.Refresh -> load(state().stage)
                is TournamentsStore.Intent.SelectStage -> {
                    dispatch(Message.StageSelected(intent.stage))
                    load(intent.stage)
                }
                is TournamentsStore.Intent.SelectMatch -> {
                    val match = findMatch(intent.matchId, state().bracket)
                    dispatch(Message.MatchSelected(match))
                }
                TournamentsStore.Intent.ResetViewport -> dispatch(Message.ViewportReset)
            }
        }

        private fun findMatch(matchId: String?, bracket: TournamentBracket?): TournamentMatch? {
            if (matchId == null || bracket == null) return null
            for (round in bracket.rounds) {
                val match = round.matches.firstOrNull { it.id == matchId }
                if (match != null) return match
            }
            return null
        }

        private fun load(stage: TournamentStage) {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading)

                when (val result = tournamentRepository.getBracket(stage)) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<TournamentsStore.State, Message> {
        override fun TournamentsStore.State.reduce(msg: Message): TournamentsStore.State = when (msg) {
            Message.Loading -> copy(isLoading = true, error = null)
            is Message.Loaded -> copy(bracket = msg.bracket, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
            is Message.StageSelected -> copy(stage = msg.stage, selectedMatch = null)
            is Message.MatchSelected -> copy(selectedMatch = msg.match)
            Message.ViewportReset -> copy(resetViewportTrigger = resetViewportTrigger + 1L)
        }
    }
}
