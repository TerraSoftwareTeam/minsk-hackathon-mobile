package com.dev.lbsuhack.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class HackStatusEmphasis {
    ACTIVE,
    WAITING,
    DONE,
    CLOSED,
}

@Composable
fun HackStatusChip(
    text: String,
    icon: ImageVector,
    emphasis: HackStatusEmphasis,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    val colors = MaterialTheme.colorScheme
    val container: Color
    val content: Color
    val border: Color
    when (emphasis) {
        HackStatusEmphasis.ACTIVE -> {
            container = colors.onSurface
            content = colors.surface
            border = colors.onSurface
        }
        HackStatusEmphasis.WAITING -> {
            container = colors.surfaceContainerHigh
            content = colors.onSurface
            border = colors.outlineVariant
        }
        HackStatusEmphasis.DONE -> {
            container = colors.surfaceContainerHigh
            content = colors.onSurface
            border = colors.onSurface
        }
        HackStatusEmphasis.CLOSED -> {
            container = Color.Transparent
            content = colors.onSurfaceVariant
            border = colors.outlineVariant
        }
    }
    Row(
        modifier = modifier
            .background(container, shape)
            .border(1.dp, border, shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (emphasis == HackStatusEmphasis.ACTIVE) FontWeight.Bold else FontWeight.SemiBold,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
