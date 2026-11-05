package com.dev.lbsuhack.feature.auth.store.register

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText

interface RegisterStore :
    Store<RegisterStore.Intent, RegisterStore.State, RegisterStore.Label> {

    sealed interface Intent {
        data class NameChanged(val name: String) : Intent
        data class EmailChanged(val email: String) : Intent
        data class PasswordChanged(val password: String) : Intent
        data object Submit : Intent
        data object BackToLogin : Intent
    }

    data class State(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label {
        data object BackToLogin : Label
    }
}
