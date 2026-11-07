package com.dev.lbsuhack.core.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HackChanceMeter(
    title: String,
    percent: Int?,
    levelText: String?,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
    description: String? = null,
    footnote: String? = null,
) {
    val progress by animateFloatAsState(
        targetValue = (percent ?: 0) / FULL_PERCENT,
        animationSpec = tween(durationMillis = FILL_MS),
        label = "HackChanceMeterProgress",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(if (prominent) 20.dp else 17.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = if (prominent) {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (prominent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (!footnote.isNullOrBlank()) {
                HackInfoButton(title = title, text = footnote)
            }
            AnimatedContent(
                targetState = percent,
                transitionSpec = { fadeIn(tween(FADE_MS)) togetherWith fadeOut(tween(FADE_MS)) },
                label = "HackChanceMeterValue",
            ) { value ->
                if (value == null) {
                    HackDotsLoader(color = MaterialTheme.colorScheme.onSurfaceVariant, dotSize = 6.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "$value%",
                            style = if (prominent) {
                                MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                            } else {
                                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (levelText != null) ChanceLevelBadge(text = levelText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (prominent) 12.dp else 8.dp))

        if (percent == null) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (prominent) 7.dp else 5.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (prominent) 7.dp else 5.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }

        if (description != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChanceLevelBadge(text: String) {
    val shape = MaterialTheme.shapes.small
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

private const val FULL_PERCENT = 100f
private const val FILL_MS = 600
private const val FADE_MS = 180
