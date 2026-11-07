package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R

@Composable
internal fun ConfirmedActivityInfo(confirmedTime: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.large,
            )
            .padding(14.dp),
    ) {
        ConfirmedInfoRow(
            icon = Icons.Outlined.Schedule,
            text = stringResource(R.string.details_confirmed_time_label) + " " + confirmedTime,
        )
    }
}
