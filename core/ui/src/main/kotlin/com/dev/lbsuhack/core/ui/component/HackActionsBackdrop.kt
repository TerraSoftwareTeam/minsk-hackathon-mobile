package com.dev.lbsuhack.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
fun HackActionsBackdrop(modifier: Modifier = Modifier) {
    val scrim = MaterialTheme.colorScheme.scrim
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                0f to scrim.copy(alpha = 0f),
                0.25f to scrim.copy(alpha = 0.12f),
                0.55f to scrim.copy(alpha = 0.5f),
                0.8f to scrim.copy(alpha = 0.75f),
                1f to scrim.copy(alpha = 0.85f),
            ),
        )
    }
}
