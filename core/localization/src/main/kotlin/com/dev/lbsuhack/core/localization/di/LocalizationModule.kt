package com.dev.lbsuhack.core.localization.di

import com.dev.lbsuhack.core.domain.localization.LocaleProvider
import com.dev.lbsuhack.core.localization.AndroidLanguageController
import com.dev.lbsuhack.core.localization.LanguageController
import org.koin.dsl.binds
import org.koin.dsl.module

val localizationModule = module {
    single { AndroidLanguageController() }
        .binds(arrayOf(LanguageController::class, LocaleProvider::class))
}
