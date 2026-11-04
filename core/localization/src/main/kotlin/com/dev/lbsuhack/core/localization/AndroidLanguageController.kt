package com.dev.lbsuhack.core.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale


class AndroidLanguageController : LanguageController {

    private val _current = MutableStateFlow(readCurrent())
    override val current: StateFlow<AppLanguage> = _current.asStateFlow()

    override val availableLanguages: List<AppLanguage> = AppLanguage.entries

    override fun setLanguage(language: AppLanguage) {
        if (_current.value == language) return

        _current.value = language
        AppCompatDelegate.setApplicationLocales(language.toLocales())
    }

    override fun refresh() {
        _current.value = readCurrent()
    }

    override fun currentLanguageTag(): String {
        val selected = _current.value
        if (selected.tag != null) return selected.tag

        val locales = AppCompatDelegate.getApplicationLocales()
        val first = if (locales.size() == 0) null else locales.get(0)
        return first?.toLanguageTag() ?: Locale.getDefault().toLanguageTag()
    }

    private fun AppLanguage.toLocales(): LocaleListCompat =
        tag?.let(LocaleListCompat::forLanguageTags)
            ?: LocaleListCompat.getEmptyLocaleList()

    private companion object {
        fun readCurrent(): AppLanguage {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.size() == 0) return AppLanguage.SYSTEM

            val language = locales.get(0)?.language ?: return AppLanguage.SYSTEM

            return when {
                language.equals("en", ignoreCase = true) -> AppLanguage.ENGLISH
                language.equals("ru", ignoreCase = true) -> AppLanguage.RUSSIAN
                else -> AppLanguage.SYSTEM
            }
        }
    }
}
