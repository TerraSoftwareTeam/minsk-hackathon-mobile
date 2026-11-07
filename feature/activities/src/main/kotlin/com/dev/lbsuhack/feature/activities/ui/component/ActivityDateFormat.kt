package com.dev.lbsuhack.feature.activities.ui.component

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun rememberActivityDateFormatter(): (OffsetDateTime) -> String {
    val locale = LocalLocale.current.platformLocale
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val formatter = remember(locale, is24Hour) {
        DateTimeFormatter.ofPattern(if (is24Hour) PATTERN_24_HOUR else PATTERN_12_HOUR, locale)
    }
    return remember(formatter) {
        { date: OffsetDateTime -> date.atZoneSameInstant(ZoneId.systemDefault()).format(formatter) }
    }
}

private const val PATTERN_24_HOUR = "d MMMM yyyy, HH:mm"
private const val PATTERN_12_HOUR = "d MMMM yyyy, h:mm a"
