package com.dev.lbsuhack.feature.auth.presentation.register

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.auth.store.register.RegisterStore
import com.dev.lbsuhack.feature.auth.store.register.RegisterStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModel(
    storeFactory: RegisterStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<RegisterStore.State> = store.stateFlow
    val labels: Flow<RegisterStore.Label> = store.labels

    fun accept(intent: RegisterStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
