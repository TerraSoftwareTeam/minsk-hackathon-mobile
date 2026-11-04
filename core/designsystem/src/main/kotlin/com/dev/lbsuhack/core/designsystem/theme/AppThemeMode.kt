package com.dev.lbsuhack.core.designsystem.theme

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

val DefaultThemeMode: AppThemeMode = AppThemeMode.DARK

fun AppThemeMode.resolveDark(systemInDark: Boolean): Boolean = when (this) {
    AppThemeMode.SYSTEM -> systemInDark
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
}
