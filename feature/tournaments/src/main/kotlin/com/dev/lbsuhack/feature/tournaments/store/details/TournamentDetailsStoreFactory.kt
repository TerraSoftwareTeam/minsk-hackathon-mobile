package com.dev.lbsuhack.feature.tournaments.store.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.tournaments.domain.Tournament
import com.dev.lbsuhack.feature.tournaments.domain.TournamentRepository
import com.dev.lbsuhack.feature.tournaments.toUiText
import kotlinx.coroutines.launch

class TournamentDetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val tournamentRepository: TournamentRepository,
) {

    fun create(): TournamentDetailsStore =
        object : TournamentDetailsStore,
            Store<TournamentDetailsStore.Intent, TournamentDetailsStore.State, Nothing> by storeFactory.create(
                name = "TournamentDetailsStore",
                initialState = TournamentDetailsStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class Loading(val tournamentId: String) : Message
        data class Loaded(val tournament: Tournament) : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<TournamentDetailsStore.Intent, Nothing, TournamentDetailsStore.State, Message, Nothing>() {

        override fun executeIntent(intent: TournamentDetailsStore.Intent) {
            when (intent) {
                is TournamentDetailsStore.Intent.Load -> load(intent.tournamentId)
                TournamentDetailsStore.Intent.Retry -> {
                    val tournamentId = state().tournamentId
                    if (tournamentId.isNotBlank()) load(tournamentId)
                }
            }
        }

        private fun load(tournamentId: String) {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading(tournamentId))

                when (val result = tournamentRepository.getTournament(tournamentId)) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<TournamentDetailsStore.State, Message> {
        override fun TournamentDetailsStore.State.reduce(msg: Message): TournamentDetailsStore.State = when (msg) {
            is Message.Loading -> copy(tournamentId = msg.tournamentId, isLoading = true, error = null)
            is Message.Loaded -> copy(tournament = msg.tournament, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
        }
    }
}
