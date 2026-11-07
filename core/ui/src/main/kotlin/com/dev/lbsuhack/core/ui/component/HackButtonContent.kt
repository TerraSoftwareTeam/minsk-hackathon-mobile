package com.dev.lbsuhack.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun HackButtonContent(
    loading: Boolean,
    modifier: Modifier = Modifier,
    loaderColor: Color = LocalContentColor.current,
    content: @Composable RowScope.() -> Unit,
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (loading) 0f else 1f,
        animationSpec = tween(durationMillis = FADE_MS),
        label = "HackButtonContentAlpha",
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
        AnimatedVisibility(
            visible = loading,
            enter = fadeIn(tween(FADE_MS)) + scaleIn(tween(FADE_MS), initialScale = 0.6f),
            exit = fadeOut(tween(FADE_MS)) + scaleOut(tween(FADE_MS), targetScale = 0.6f),
        ) {
            HackDotsLoader(color = loaderColor)
        }
    }
}

private const val FADE_MS = 180
