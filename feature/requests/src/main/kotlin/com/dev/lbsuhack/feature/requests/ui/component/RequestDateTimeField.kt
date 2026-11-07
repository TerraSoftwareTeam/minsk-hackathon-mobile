package com.dev.lbsuhack.feature.requests.ui.component

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import com.dev.lbsuhack.feature.requests.ui.labels.formatRequestDate
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

@Composable
internal fun RequestDateTimeField(
    startsAt: OffsetDateTime,
    enabled: Boolean,
    onChange: (OffsetDateTime) -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val is24Hour = DateFormat.is24HourFormat(context)
    val zone = ZoneId.systemDefault()
    val date = startsAt.atZoneSameInstant(zone)

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            onChange(
                                LocalDate.of(year, month + 1, day).atTime(hour, minute)
                                    .atZone(zone).toOffsetDateTime(),
                            )
                        },
                        date.hour,
                        date.minute,
                        is24Hour,
                    ).show()
                },
                date.year,
                date.monthValue - 1,
                date.dayOfMonth,
            ).apply {
                datePicker.minDate = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
            }.show()
        },
    ) {
        Text(formatRequestDate(startsAt, locale, is24Hour))
    }
}
