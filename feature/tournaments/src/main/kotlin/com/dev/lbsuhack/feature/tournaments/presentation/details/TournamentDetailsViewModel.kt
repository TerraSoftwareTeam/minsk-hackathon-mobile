package com.dev.lbsuhack.feature.tournaments.presentation.details

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.tournaments.store.details.TournamentDetailsStore
import com.dev.lbsuhack.feature.tournaments.store.details.TournamentDetailsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class TournamentDetailsViewModel(
    storeFactory: TournamentDetailsStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<TournamentDetailsStore.State> = store.stateFlow

    fun accept(intent: TournamentDetailsStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
