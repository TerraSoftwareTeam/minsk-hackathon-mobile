package com.dev.lbsuhack.core.localization.language

import androidx.annotation.StringRes
import com.dev.lbsuhack.core.localization.R


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
