package com.dev.lbsuhack.feature.createvote.presentation.createvote

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.createvote.store.createvote.CreateVoteStore
import com.dev.lbsuhack.feature.createvote.store.createvote.CreateVoteStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class CreateVoteViewModel(
    storeFactory: CreateVoteStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<CreateVoteStore.State> = store.stateFlow
    val labels: Flow<CreateVoteStore.Label> = store.labels

    fun accept(intent: CreateVoteStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
