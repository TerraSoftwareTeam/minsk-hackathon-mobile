package com.dev.lbsuhack.core.designsystem.theme

import kotlinx.coroutines.flow.StateFlow

interface ThemeController {

    val availableModes: List<AppThemeMode>

    val current: StateFlow<AppThemeMode>

    fun setMode(mode: AppThemeMode)
}

class DefaultThemeController(
    private val preferences: ThemePreferences,
) : ThemeController {

    override val availableModes: List<AppThemeMode> = AppThemeMode.entries

    override val current: StateFlow<AppThemeMode> = preferences.mode

    override fun setMode(mode: AppThemeMode) {
        preferences.setMode(mode)
    }
}
