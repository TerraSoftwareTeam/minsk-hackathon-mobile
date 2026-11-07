package com.dev.lbsuhack.feature.activities.ui.component

internal fun initials(name: String): String =
    name.split(' ').filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

internal fun shortName(name: String): String {
    val parts = name.split(' ').filter(String::isNotBlank)
    return when {
        parts.isEmpty() -> name
        parts.size == 1 -> parts[0]
        else -> "${parts[0]} ${parts[1].first().uppercaseChar()}."
    }
}
