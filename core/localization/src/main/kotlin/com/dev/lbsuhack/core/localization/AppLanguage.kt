package com.dev.lbsuhack.core.localization

import androidx.annotation.StringRes


enum class AppLanguage(
    val tag: String?,
    @param:StringRes val labelRes: Int,
) {
    SYSTEM(
        tag = null,
        labelRes = R.string.localization_language_system,
    ),
    ENGLISH(
        tag = "en",
        labelRes = R.string.localization_language_english,
    ),
    RUSSIAN(
        tag = "ru",
        labelRes = R.string.localization_language_russian,
    ),
}
