package com.dev.lbsuhack.feature.profile.presentation.settings

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.profile.store.settings.SettingsStore
import com.dev.lbsuhack.feature.profile.store.settings.SettingsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    storeFactory: SettingsStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<SettingsStore.State> = store.stateFlow

    init {
        store.accept(SettingsStore.Intent.Init)
    }

    fun accept(intent: SettingsStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
