package com.dev.lbsuhack.feature.profile.presentation.profile

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStore
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    storeFactory: ProfileStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<ProfileStore.State> = store.stateFlow

    fun accept(intent: ProfileStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
