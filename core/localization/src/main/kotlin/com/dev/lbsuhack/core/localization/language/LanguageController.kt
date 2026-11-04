package com.dev.lbsuhack.core.localization.language

import com.dev.lbsuhack.core.domain.localization.LocaleProvider
import kotlinx.coroutines.flow.StateFlow

interface LanguageController : LocaleProvider {

    val availableLanguages: List<AppLanguage>

    val current: StateFlow<AppLanguage>

    fun setLanguage(language: AppLanguage)

    fun refresh()

    fun resetToSystem() {
        setLanguage(AppLanguage.SYSTEM)
    }
}
