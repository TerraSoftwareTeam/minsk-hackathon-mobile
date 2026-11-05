package com.dev.lbsuhack.feature.activities.presentation.activities

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesViewModel(
    storeFactory: ActivitiesStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<ActivitiesStore.State> = store.stateFlow
    val labels: Flow<ActivitiesStore.Label> = store.labels

    fun accept(intent: ActivitiesStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
