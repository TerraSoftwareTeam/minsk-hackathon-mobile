package com.dev.lbsuhack.feature.profile.ui.settings

import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.feature.profile.R

fun AppThemeMode.labelRes(): Int = when (this) {
    AppThemeMode.SYSTEM -> R.string.profile_theme_system
    AppThemeMode.LIGHT -> R.string.profile_theme_light
    AppThemeMode.DARK -> R.string.profile_theme_dark
}
