package com.dev.lbsuhack.feature.profile.util

import com.dev.lbsuhack.core.localization.language.AppLanguage
import com.dev.lbsuhack.core.localization.language.LanguageController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLanguageController(
    initial: AppLanguage = AppLanguage.SYSTEM,
) : LanguageController {

    private val _current = MutableStateFlow(initial)

    override val availableLanguages: List<AppLanguage> = AppLanguage.entries
    override val current: StateFlow<AppLanguage> = _current.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        _current.value = language
    }

    override fun refresh() = Unit

    override fun currentLanguageTag(): String = _current.value.tag ?: "en"
}
