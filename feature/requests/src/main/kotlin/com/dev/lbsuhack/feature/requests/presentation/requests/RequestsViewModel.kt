package com.dev.lbsuhack.feature.requests.presentation.requests

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class RequestsViewModel(
    storeFactory: RequestsStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<RequestsStore.State> = store.stateFlow
    val labels: Flow<RequestsStore.Label> = store.labels

    fun accept(intent: RequestsStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
