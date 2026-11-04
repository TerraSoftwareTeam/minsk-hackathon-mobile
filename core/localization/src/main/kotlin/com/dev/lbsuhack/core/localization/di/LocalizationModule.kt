package com.dev.lbsuhack.core.localization.di

import com.dev.lbsuhack.core.domain.localization.LocaleProvider
import com.dev.lbsuhack.core.localization.language.AndroidLanguageController
import com.dev.lbsuhack.core.localization.language.LanguageController
import org.koin.dsl.binds
import org.koin.dsl.module

val localizationModule = module {
    single { AndroidLanguageController() }
        .binds(arrayOf(LanguageController::class, LocaleProvider::class))
}
