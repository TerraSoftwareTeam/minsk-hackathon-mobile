package com.dev.lbsuhack.feature.tournaments.presentation.tournaments

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.tournaments.store.tournaments.TournamentsStore
import com.dev.lbsuhack.feature.tournaments.store.tournaments.TournamentsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class TournamentsViewModel(
    storeFactory: TournamentsStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<TournamentsStore.State> = store.stateFlow
    val labels: Flow<TournamentsStore.Label> = store.labels

    fun accept(intent: TournamentsStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
