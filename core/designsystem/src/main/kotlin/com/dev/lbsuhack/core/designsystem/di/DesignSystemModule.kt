package com.dev.lbsuhack.core.designsystem.di

import com.dev.lbsuhack.core.designsystem.mode.DefaultThemeController
import com.dev.lbsuhack.core.designsystem.mode.PrefsThemePreferences
import com.dev.lbsuhack.core.designsystem.mode.ThemeController
import com.dev.lbsuhack.core.designsystem.mode.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val designSystemModule = module {
    single<ThemePreferences> { PrefsThemePreferences(androidContext()) }
    single<ThemeController> { DefaultThemeController(get()) }
}
