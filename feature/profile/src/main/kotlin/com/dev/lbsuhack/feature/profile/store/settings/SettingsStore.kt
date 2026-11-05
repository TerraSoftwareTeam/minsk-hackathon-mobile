package com.dev.lbsuhack.feature.profile.store.settings

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.localization.language.AppLanguage

interface SettingsStore : Store<SettingsStore.Intent, SettingsStore.State, Nothing> {

    sealed interface Intent {
        data class SetTheme(val mode: AppThemeMode) : Intent
        data class SetLanguage(val language: AppLanguage) : Intent
    }

    data class State(
        val theme: AppThemeMode,
        val language: AppLanguage,
    )
}
