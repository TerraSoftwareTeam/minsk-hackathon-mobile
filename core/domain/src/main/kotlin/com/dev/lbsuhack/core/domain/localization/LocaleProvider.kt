package com.dev.lbsuhack.core.domain.localization

/**
 * Provides the BCP-47 language tag of the currently selected application language.
 * Used by non-UI layers (for example, the network layer for the Accept-Language header).
 */
interface LocaleProvider {
    fun currentLanguageTag(): String
}
