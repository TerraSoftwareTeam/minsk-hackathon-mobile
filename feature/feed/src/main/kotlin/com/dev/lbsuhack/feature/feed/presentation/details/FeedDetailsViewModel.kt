package com.dev.lbsuhack.feature.feed.presentation.details

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.feed.store.details.FeedDetailsStore
import com.dev.lbsuhack.feature.feed.store.details.FeedDetailsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class FeedDetailsViewModel(
    storeFactory: FeedDetailsStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<FeedDetailsStore.State> = store.stateFlow

    fun accept(intent: FeedDetailsStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
