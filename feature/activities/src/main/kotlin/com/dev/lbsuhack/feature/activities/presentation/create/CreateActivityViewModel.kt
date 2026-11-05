package com.dev.lbsuhack.feature.activities.presentation.create

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStore
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class CreateActivityViewModel(
    storeFactory: CreateActivityStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<CreateActivityStore.State> = store.stateFlow
    val labels: Flow<CreateActivityStore.Label> = store.labels

    fun accept(intent: CreateActivityStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
