package com.dev.lbsuhack.core.localization.text

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes


sealed interface UiText {

    data class Resource(
        @param:StringRes val id: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    data class Plural(
        @param:PluralsRes val id: Int,
        val count: Int,
        val args: List<Any> = listOf(count),
    ) : UiText

    data class Raw(val value: String) : UiText
}
