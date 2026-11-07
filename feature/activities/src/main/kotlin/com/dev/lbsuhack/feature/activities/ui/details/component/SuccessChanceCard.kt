package com.dev.lbsuhack.feature.activities.ui.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.ui.component.ActivityChanceMeter

@Composable
internal fun SuccessChanceCard(probability: Double, modifier: Modifier = Modifier) {
    ActivityChanceMeter(
        probability = probability,
        prominent = true,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge)
            .padding(18.dp),
    )
}
