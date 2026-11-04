package com.dev.lbsuhack.core.designsystem.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ThemePreferences {

    val mode: StateFlow<AppThemeMode>

    fun setMode(mode: AppThemeMode)
}


class PrefsThemePreferences(context: Context) : ThemePreferences {

    private val preferences = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(
        ThemeModeCodec.decode(preferences.getString(KEY_MODE, null)),
    )

    override val mode: StateFlow<AppThemeMode> = _mode.asStateFlow()

    override fun setMode(mode: AppThemeMode) {
        if (_mode.value == mode) return

        _mode.value = mode
        preferences.edit().putString(KEY_MODE, ThemeModeCodec.encode(mode)).apply()
    }

    private companion object {
        const val FILE_NAME = "app_theme"
        const val KEY_MODE = "theme_mode"
    }
}
