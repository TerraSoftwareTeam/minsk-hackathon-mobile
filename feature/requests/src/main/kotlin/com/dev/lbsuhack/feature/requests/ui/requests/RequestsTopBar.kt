package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.feature.requests.R

@Composable
internal fun RequestsTopBar(
    title: String,
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    HackTopBar(
        title = title,
        onBack = onBack,
        backContentDescription = stringResource(R.string.requests_back),
        actions = {
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.Refresh, stringResource(R.string.requests_refresh))
                }
            }
            IconButton(onClick = onOpenProfile) {
                Icon(Icons.Default.Person, stringResource(R.string.requests_profile))
            }
        },
    )
}
