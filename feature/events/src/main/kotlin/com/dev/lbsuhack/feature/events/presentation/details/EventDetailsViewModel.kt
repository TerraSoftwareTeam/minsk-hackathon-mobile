package com.dev.lbsuhack.feature.events.presentation.details

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.events.store.details.EventDetailsStore
import com.dev.lbsuhack.feature.events.store.details.EventDetailsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailsViewModel(
    storeFactory: EventDetailsStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<EventDetailsStore.State> = store.stateFlow

    fun accept(intent: EventDetailsStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
