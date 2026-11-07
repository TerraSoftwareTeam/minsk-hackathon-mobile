package com.dev.lbsuhack.feature.activities.ui.details.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.localization.text.asString
import com.dev.lbsuhack.core.ui.component.HackEmptyState
import com.dev.lbsuhack.core.ui.component.HackLoading
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.store.details.ActivityLoadState

@Composable
internal fun DetailsStateView(
    loadState: ActivityLoadState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (loadState) {
        ActivityLoadState.Idle,
        ActivityLoadState.Loading -> {
            HackLoading(modifier = modifier.fillMaxSize())
        }

        ActivityLoadState.NotFound -> {
            HackEmptyState(
                title = stringResource(R.string.details_not_found_title),
                description = stringResource(R.string.details_not_found_desc),
                icon = Icons.Outlined.SearchOff,
                modifier = modifier.fillMaxSize(),
            )
        }

        ActivityLoadState.Forbidden -> {
            HackEmptyState(
                title = stringResource(R.string.details_forbidden_title),
                description = stringResource(R.string.details_forbidden_desc),
                icon = Icons.Outlined.Block,
                modifier = modifier.fillMaxSize(),
            )
        }

        is ActivityLoadState.Failed -> {
            HackEmptyState(
                title = stringResource(R.string.details_error_title),
                description = loadState.error.asString(),
                icon = Icons.Outlined.CloudOff,
                modifier = modifier.fillMaxSize(),
                action = {
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.activities_retry))
                    }
                },
            )
        }
    }
}
