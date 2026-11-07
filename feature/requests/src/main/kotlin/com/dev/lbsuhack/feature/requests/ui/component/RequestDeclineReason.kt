package com.dev.lbsuhack.feature.requests.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.requests.R

@Composable
internal fun RequestDeclineReason(
    reason: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.requests_decline_reason_format, reason),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.large,
            )
            .padding(14.dp),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
