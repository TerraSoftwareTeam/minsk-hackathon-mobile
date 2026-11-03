package com.dev.lbsuhack.feature.home.presentation

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.home.store.HomeStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val store: HomeStore,
) : ViewModel() {

    val state: StateFlow<HomeStore.State> = store.stateFlow
    val labels: Flow<HomeStore.Label> = store.labels

    fun accept(intent: HomeStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
