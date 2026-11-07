package com.dev.lbsuhack.feature.profile.ui.profile.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.core.ui.component.HackEmptyState
import com.dev.lbsuhack.feature.profile.R

@Composable
fun ProfileErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        HackEmptyState(
            title = stringResource(R.string.profile_error_load_title),
            description = stringResource(R.string.profile_error_load_desc),
            icon = Icons.Default.CloudOff,
            action = {
                Button(
                    onClick = onRetry,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.profile_retry))
                }
            },
        )
    }
}
