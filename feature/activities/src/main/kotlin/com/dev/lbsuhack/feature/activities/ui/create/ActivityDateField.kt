package com.dev.lbsuhack.feature.activities.ui.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalLocale

@Composable
internal fun ActivityDateField(startsAt: String, enabled: Boolean, onChange: (String) -> Unit) {
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val date = OffsetDateTime.parse(startsAt).atZoneSameInstant(ZoneId.systemDefault())
    OutlinedButton(enabled = enabled, onClick = {
        DatePickerDialog(context, { _, year, month, day ->
            TimePickerDialog(context, { _, hour, minute ->
                onChange(LocalDate.of(year, month + 1, day).atTime(hour, minute)
                    .atZone(ZoneId.systemDefault()).toOffsetDateTime().toString())
            }, date.hour, date.minute, true).show()
        }, date.year, date.monthValue - 1, date.dayOfMonth).show()
    }) {
        Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", locale)))
    }
}
