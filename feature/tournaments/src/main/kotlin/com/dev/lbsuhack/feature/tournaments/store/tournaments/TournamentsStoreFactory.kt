package com.dev.lbsuhack.feature.tournaments.store.tournaments

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
        data class Loaded(val tournaments: List<Tournament>) : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<TournamentsStore.Intent, Nothing, TournamentsStore.State, Message, TournamentsStore.Label>() {

        override fun executeIntent(intent: TournamentsStore.Intent) {
            when (intent) {
                TournamentsStore.Intent.Load, TournamentsStore.Intent.Refresh -> load()
                is TournamentsStore.Intent.ClickTournament ->
                    publish(TournamentsStore.Label.OpenDetails(intent.tournamentId))
            }
        }

        private fun load() {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading)

                when (val result = tournamentRepository.getTournaments()) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<TournamentsStore.State, Message> {
        override fun TournamentsStore.State.reduce(msg: Message): TournamentsStore.State = when (msg) {
            Message.Loading -> copy(isLoading = true, error = null)
            is Message.Loaded -> copy(tournaments = msg.tournaments, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
        }
    }
}
