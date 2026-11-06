package com.dev.lbsuhack.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.designsystem.theme.HackTheme

@Composable
fun HackEventCard(
    day: String,
    month: String,
    category: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    time: String? = null,
    place: String? = null,
    participantsLabel: String? = null,
    participantsText: String? = null,
    participantsProgress: Float? = null,
    capacityBadgeText: String? = null,
    isFull: Boolean = false,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    accentContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick ?: onActionClick ?: {},
        enabled = onClick != null || onActionClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(
                    modifier = Modifier.size(width = 60.dp, height = 68.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = accentContainerColor,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.titleLarge,
                            color = accentColor,
                        )
                        Text(
                            text = month.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            maxLines = 1,
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (!summary.isNullOrBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (time != null || place != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (time != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    if (place != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = place,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            if (participantsProgress != null || capacityBadgeText != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (participantsLabel != null || participantsText != null || capacityBadgeText != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (participantsLabel != null) {
                                Text(
                                    text = participantsLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (participantsText != null) {
                                Text(
                                    text = participantsText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (capacityBadgeText != null) {
                                val background = if (isFull) {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                }
                                val foreground = if (isFull) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = background,
                                ) {
                                    Text(
                                        text = capacityBadgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = foreground,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }

                    if (participantsProgress != null) {
                        LinearProgressIndicator(
                            progress = { participantsProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = accentColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            strokeCap = StrokeCap.Round,
                        )
                    }
                }
            }

            if (actionText != null) {
                Button(
                    onClick = onActionClick ?: onClick ?: {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Preview(
    name = "HackEventCard · dark",
    group = "Shared UI",
    showBackground = true,
)
@Composable
private fun HackEventCardDarkPreview() {
    HackTheme(mode = AppThemeMode.DARK) {
        HackEventCard(
            day = "24",
            month = "ноя",
            category = "ХАКАТОН",
            title = "Всероссийский IT-Хакатон 2026",
            summary = "Разработка инновационных сервисов для школьной среды",
            time = "10:00 – 19:00",
            place = "Главный корпус, ауд. 301",
            participantsLabel = "Участники",
            participantsText = "18 / 30",
            participantsProgress = 0.6f,
            capacityBadgeText = "12 мест",
            actionText = "Открыть событие",
        )
    }
}

@Preview(
    name = "HackEventCard · light",
    group = "Shared UI",
    showBackground = true,
)
@Composable
private fun HackEventCardLightPreview() {
    HackTheme(mode = AppThemeMode.LIGHT) {
        HackEventCard(
            day = "24",
            month = "ноя",
            category = "ХАКАТОН",
            title = "Всероссийский IT-Хакатон 2026",
            summary = "Разработка инновационных сервисов для школьной среды",
            time = "10:00 – 19:00",
            place = "Главный корпус, ауд. 301",
            participantsLabel = "Участники",
            participantsText = "18 / 30",
            participantsProgress = 0.6f,
            capacityBadgeText = "12 мест",
            actionText = "Открыть событие",
        )
    }
}
