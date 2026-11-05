package com.dev.lbsuhack.feature.feed.presentation.feed

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.feed.store.feed.FeedStore
import com.dev.lbsuhack.feature.feed.store.feed.FeedStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(
    storeFactory: FeedStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<FeedStore.State> = store.stateFlow
    val labels: Flow<FeedStore.Label> = store.labels

    fun accept(intent: FeedStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
