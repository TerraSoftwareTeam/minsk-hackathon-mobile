package com.dev.lbsuhack.core.designsystem.mode

enum class AppThemeMode {
    DARK,
    LIGHT,
}

val DefaultThemeMode: AppThemeMode = AppThemeMode.DARK

fun AppThemeMode.resolveDark(): Boolean = when (this) {
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
}
