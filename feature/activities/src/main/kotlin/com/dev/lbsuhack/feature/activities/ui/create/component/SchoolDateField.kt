package com.dev.lbsuhack.feature.activities.ui.create.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackDateTimeField
import com.dev.lbsuhack.core.ui.component.HackSectionLabel
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.ui.component.rememberActivityDateFormatter
import java.time.OffsetDateTime

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
        HackSectionLabel(text = stringResource(R.string.create_activity_date_label))
        HackDateTimeField(
            value = startsAt,
            text = format(startsAt),
            onChange = onChange,
            enabled = enabled,
            isError = hasError,
        )
        Text(
            text = stringResource(
                if (hasError) R.string.create_activity_error_date_in_past else R.string.create_activity_date_hint,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
