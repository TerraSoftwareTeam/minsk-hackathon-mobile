package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.core.ui.component.HackFloatingActionButton

@Composable
internal fun ActivitiesBottomActions(
    actionLabel: String,
    onOpenDetails: (String) -> Unit,
    onScanError: (UiText) -> Unit,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActivityScanButton(
            onOpenDetails = onOpenDetails,
            onError = onScanError,
            modifier = Modifier.weight(1f).height(72.dp),
        )
        HackFloatingActionButton(
            text = actionLabel,
            icon = Icons.Default.Add,
            onClick = onPrimaryAction,
            modifier = Modifier.weight(1f).height(72.dp),
        )
    }
}
