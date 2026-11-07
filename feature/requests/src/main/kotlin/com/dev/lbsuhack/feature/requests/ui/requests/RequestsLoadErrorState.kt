package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dev.lbsuhack.core.ui.component.HackEmptyState
import com.dev.lbsuhack.feature.requests.R

@Composable
internal fun RequestsLoadErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HackEmptyState(
        title = stringResource(R.string.requests_empty_error_title),
        description = stringResource(R.string.requests_empty_error_desc),
        icon = Icons.Outlined.CloudOff,
        modifier = modifier,
        action = {
            Button(onClick = onRetry, shape = MaterialTheme.shapes.medium) {
                Text(text = stringResource(R.string.requests_retry), fontWeight = FontWeight.Bold)
            }
        },
    )
}
