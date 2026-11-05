package com.dev.lbsuhack.feature.tournaments.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dev.lbsuhack.core.designsystem.theme.hack

@Composable
internal fun BoardGridBackground(
    scale: Float,
    panOffset: Offset,
    modifier: Modifier = Modifier,
    dotColor: Color = MaterialTheme.hack.gridLine,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = backgroundColor)

        val baseStepPx = 36f
        var stepPx = baseStepPx * scale
        while (stepPx < 18f) {
            stepPx *= 2f
        }
        while (stepPx > 72f) {
            stepPx /= 2f
        }

        val dotRadiusPx = (1.5f * scale).coerceIn(1f, 2.5f)

        val offsetX = panOffset.x.positiveMod(stepPx)
        val offsetY = panOffset.y.positiveMod(stepPx)

        var x = offsetX
        while (x < size.width) {
            var y = offsetY
            while (y < size.height) {
                drawCircle(
                    color = dotColor.copy(alpha = 0.65f),
                    radius = dotRadiusPx,
                    center = Offset(x, y),
                )
                y += stepPx
            }
            x += stepPx
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, backgroundColor.copy(alpha = 0.5f)),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = maxOf(size.width, size.height) * 0.75f,
            ),
        )
    }
}

private fun Float.positiveMod(other: Float): Float {
    val res = this % other
    return if (res < 0f) res + other else res
}
