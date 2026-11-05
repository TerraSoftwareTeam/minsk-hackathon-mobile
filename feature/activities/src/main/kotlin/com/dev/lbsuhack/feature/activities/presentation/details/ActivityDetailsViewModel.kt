package com.dev.lbsuhack.feature.activities.presentation.details

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStore
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityDetailsViewModel(
    storeFactory: ActivityDetailsStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<ActivityDetailsStore.State> = store.stateFlow
    val labels: Flow<ActivityDetailsStore.Label> = store.labels

    fun accept(intent: ActivityDetailsStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
