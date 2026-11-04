package com.dev.lbsuhack.core.designsystem.theme


object ThemeModeCodec {

    fun encode(mode: AppThemeMode): String = mode.name

    fun decode(raw: String?): AppThemeMode =
        AppThemeMode.entries.firstOrNull { it.name == raw } ?: DefaultThemeMode
}
