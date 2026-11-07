package com.dev.lbsuhack.feature.requests.ui.labels

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun rememberRequestDateFormatter(): (OffsetDateTime) -> String {
    val locale = LocalLocale.current.platformLocale
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    return remember(locale, is24Hour) {
        { date -> formatRequestDate(date, locale, is24Hour) }
    }
}

internal fun formatRequestDate(
    date: OffsetDateTime,
    locale: Locale,
    is24Hour: Boolean,
): String {
    val pattern = if (is24Hour) DATE_PATTERN_24_HOUR else DATE_PATTERN_12_HOUR
    return date.atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(pattern, locale))
}

private const val DATE_PATTERN_24_HOUR = "d MMMM yyyy, HH:mm"
private const val DATE_PATTERN_12_HOUR = "d MMMM yyyy, h:mm a"
