package com.dev.lbsuhack.core.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HackDotsLoader(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    dotSize: Dp = 7.dp,
) {
    val transition = rememberInfiniteTransition(label = "HackDotsLoader")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize * DOT_GAP_RATIO),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DOT_COUNT) { index ->
            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = CYCLE_MS
                        0f at 0 using FastOutSlowInEasing
                        1f at RISE_MS using FastOutSlowInEasing
                        0f at RISE_MS * 2
                        0f at CYCLE_MS
                    },
                    initialStartOffset = StartOffset(index * STAGGER_MS),
                ),
                label = "HackDotsLoader$index",
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        val scale = MIN_SCALE + (1f - MIN_SCALE) * progress
                        scaleX = scale
                        scaleY = scale
                        alpha = MIN_ALPHA + (1f - MIN_ALPHA) * progress
                        translationY = -size.height * LIFT_RATIO * progress
                    }
                    .background(color = color, shape = CircleShape),
            )
        }
    }
}

private const val DOT_COUNT = 3
private const val CYCLE_MS = 1100
private const val RISE_MS = 280
private const val STAGGER_MS = 150
private const val MIN_SCALE = 0.65f
private const val MIN_ALPHA = 0.4f
private const val LIFT_RATIO = 0.45f
private const val DOT_GAP_RATIO = 0.75f
