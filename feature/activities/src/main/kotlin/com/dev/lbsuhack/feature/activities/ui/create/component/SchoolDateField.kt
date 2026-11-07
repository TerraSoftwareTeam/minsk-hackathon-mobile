package com.dev.lbsuhack.feature.activities.ui.create.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackInfoButton
import com.dev.lbsuhack.core.ui.component.HackDateTimeField
import com.dev.lbsuhack.core.ui.component.HackSectionLabel
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.store.create.earliestStartsAt
import com.dev.lbsuhack.feature.activities.store.create.votingClosesAt
import com.dev.lbsuhack.feature.activities.ui.component.rememberActivityDateFormatter
import java.time.OffsetDateTime
import java.time.ZoneId

@Composable
internal fun SchoolDateField(
    startsAt: OffsetDateTime,
    hasError: Boolean,
    enabled: Boolean,
    onChange: (OffsetDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = rememberActivityDateFormatter()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HackSectionLabel(
                text = stringResource(R.string.create_activity_date_label),
                modifier = Modifier.weight(1f),
            )
            HackInfoButton(
                title = stringResource(R.string.create_activity_date_label),
                text = stringResource(R.string.create_activity_date_hint, format(votingClosesAt(startsAt))),
            )
        }
        HackDateTimeField(
            value = startsAt,
            text = format(startsAt),
            onChange = onChange,
            enabled = enabled,
            isError = hasError,
            minDate = earliestStartsAt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDate(),
        )
        if (hasError) {
            Text(
                text = stringResource(R.string.create_activity_error_date_in_past),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
