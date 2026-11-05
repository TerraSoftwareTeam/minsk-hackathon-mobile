package com.dev.lbsuhack.feature.events.presentation.events

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.events.store.events.EventsStore
import com.dev.lbsuhack.feature.events.store.events.EventsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModel(
    storeFactory: EventsStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<EventsStore.State> = store.stateFlow
    val labels: Flow<EventsStore.Label> = store.labels

    fun accept(intent: EventsStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
