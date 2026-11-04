package com.dev.lbsuhack.core.designsystem.di

import com.dev.lbsuhack.core.designsystem.theme.DefaultThemeController
import com.dev.lbsuhack.core.designsystem.theme.PrefsThemePreferences
import com.dev.lbsuhack.core.designsystem.theme.ThemeController
import com.dev.lbsuhack.core.designsystem.theme.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val designSystemModule = module {
    single<ThemePreferences> { PrefsThemePreferences(androidContext()) }
    single<ThemeController> { DefaultThemeController(get()) }
}
