package com.dev.lbsuhack.feature.profile.util

import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.designsystem.mode.ThemeController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeThemeController(
    initial: AppThemeMode = AppThemeMode.DARK,
) : ThemeController {

    private val _current = MutableStateFlow(initial)

    override val availableModes: List<AppThemeMode> = AppThemeMode.entries
    override val current: StateFlow<AppThemeMode> = _current.asStateFlow()

    override fun setMode(mode: AppThemeMode) {
        _current.value = mode
    }
}
