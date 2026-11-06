package com.dev.lbsuhack.feature.activevote.presentation.activevote

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.activevote.store.activevote.ActiveVoteStore
import com.dev.lbsuhack.feature.activevote.store.activevote.ActiveVoteStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveVoteViewModel(
    storeFactory: ActiveVoteStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<ActiveVoteStore.State> = store.stateFlow
    val labels: Flow<ActiveVoteStore.Label> = store.labels

    fun accept(intent: ActiveVoteStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
