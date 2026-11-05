package com.dev.lbsuhack.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.store.login.LoginStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModel(
    storeFactory: LoginStoreFactory,
) : ViewModel() {

    private val store = storeFactory.create()

    val state: StateFlow<LoginStore.State> = store.stateFlow
    val labels: Flow<LoginStore.Label> = store.labels

    fun accept(intent: LoginStore.Intent) {
        store.accept(intent)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
