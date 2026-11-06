package com.dev.lbsuhack.feature.auth.store.login

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.UiText

interface LoginStore : Store<LoginStore.Intent, LoginStore.State, LoginStore.Label> {

    sealed interface Intent {
        data class UsernameChanged(val username: String) : Intent
        data class PasswordChanged(val password: String) : Intent
        data class SurnameChanged(val surname: String) : Intent
        data class NameChanged(val name: String) : Intent
        data class ClassNameChanged(val className: String) : Intent
        data class RoleChanged(val role: UserRole) : Intent
        data object Submit : Intent
    }

    data class State(
        val username: String = "",
        val password: String = "",
        val surname: String = "",
        val name: String = "",
        val className: String = "",
        val role: UserRole = UserRole.PUPIL,
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label
}
