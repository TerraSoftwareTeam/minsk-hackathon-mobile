package com.dev.lbsuhack.feature.profile.presentation

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.profile.store.ProfileStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val store: ProfileStore,
) : ViewModel() {

    val state: StateFlow<ProfileStore.State> = store.stateFlow
    val labels: Flow<ProfileStore.Label> = store.labels

    fun accept(intent: ProfileStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
