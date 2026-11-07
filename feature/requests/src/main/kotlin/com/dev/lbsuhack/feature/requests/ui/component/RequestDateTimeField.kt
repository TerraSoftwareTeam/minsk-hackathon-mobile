package com.dev.lbsuhack.feature.requests.ui.component

import androidx.compose.runtime.Composable
import com.dev.lbsuhack.core.ui.component.HackDateTimeField
import com.dev.lbsuhack.feature.requests.ui.labels.rememberRequestDateFormatter
import java.time.OffsetDateTime

@Composable
internal fun RequestDateTimeField(
    startsAt: OffsetDateTime,
    enabled: Boolean,
    onChange: (OffsetDateTime) -> Unit,
) {
    val format = rememberRequestDateFormatter()
    HackDateTimeField(
        value = startsAt,
        text = format(startsAt),
        onChange = onChange,
        enabled = enabled,
    )
}
