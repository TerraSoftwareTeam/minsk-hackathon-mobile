package com.dev.lbsuhack.feature.tournaments.ui.board

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.lbsuhack.core.designsystem.theme.HackMonoTextStyle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.tournaments.domain.MatchStatus
import com.dev.lbsuhack.feature.tournaments.domain.TournamentMatch
import com.dev.lbsuhack.feature.tournaments.domain.TournamentTeam

@Composable
internal fun TournamentMatchCard(
    match: TournamentMatch,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_pulse")
    val livePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "live_pulse",
    )

    val targetBorderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        match.status == MatchStatus.LIVE -> MaterialTheme.hack.success.copy(alpha = livePulseAlpha)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 180),
        label = "border_color",
    )

    val targetBorderWidth = if (isSelected || match.status == MatchStatus.LIVE) 1.5.dp else 1.dp
    val borderWidth by animateDpAsState(
        targetValue = targetBorderWidth,
        animationSpec = tween(durationMillis = 180),
        label = "border_width",
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 1.dp,
        animationSpec = tween(durationMillis = 180),
        label = "elevation",
    )

    Card(
        modifier = modifier
            .size(width = 240.dp, height = 110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            MatchHeader(
                match = match,
                livePulseAlpha = livePulseAlpha,
            )

            TeamRow(
                team = match.teamA,
                isWinner = match.winnerId == match.teamA.id,
                isLive = match.status == MatchStatus.LIVE,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
            )

            if (match.teamB == null || match.teamB.isBye) {
                ByeTeamRow()
            } else {
                TeamRow(
                    team = match.teamB,
                    isWinner = match.winnerId == match.teamB.id,
                    isLive = match.status == MatchStatus.LIVE,
                )
            }
        }
    }
}

@Composable
private fun MatchHeader(
    match: TournamentMatch,
    livePulseAlpha: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (match.status == MatchStatus.LIVE) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.hack.success.copy(alpha = livePulseAlpha)),
                )
                Text(
                    text = "LIVE // ИДЁТ СЕЙЧАС",
                    style = HackMonoTextStyle.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = MaterialTheme.hack.success,
                )
            }
        } else {
            val (label, containerColor, contentColor) = when (match.status) {
                MatchStatus.BYE -> Triple(
                    "BYE // АВТОПРОХОД",
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.secondary,
                )
                MatchStatus.COMPLETED -> Triple(
                    "ФИНАЛ",
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MatchStatus.SCHEDULED -> Triple(
                    "ОЖИДАНИЕ",
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                MatchStatus.LIVE -> Triple("", Color.Transparent, Color.Transparent)
            }

            Surface(
                shape = RoundedCornerShape(2.dp),
                color = containerColor,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    text = label,
                    style = HackMonoTextStyle.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
        }

        Text(
            text = "#${match.roundIndex + 1}.${match.matchIndex + 1}",
            style = HackMonoTextStyle.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun TeamRow(
    team: TournamentTeam,
    isWinner: Boolean,
    isLive: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(2.dp),
                color = if (isWinner) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(
                    1.dp,
                    if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = team.name.take(2).uppercase(),
                        style = HackMonoTextStyle.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (isWinner) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = team.name,
                    style = HackMonoTextStyle.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isWinner) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (team.subtitle != null) {
                    Text(
                        text = team.subtitle,
                        style = HackMonoTextStyle.copy(
                            fontSize = 9.sp,
                            letterSpacing = 0.2.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        val scoreText = team.score?.toString() ?: "—"
        val scoreColor = when {
            isWinner -> MaterialTheme.hack.success
            isLive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Surface(
            shape = RoundedCornerShape(2.dp),
            color = if (isWinner) MaterialTheme.hack.success.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                1.dp,
                if (isWinner) MaterialTheme.hack.success.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            Text(
                text = scoreText,
                style = HackMonoTextStyle.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isWinner || isLive) FontWeight.Bold else FontWeight.Normal,
                ),
                color = scoreColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun ByeTeamRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "—",
                        style = HackMonoTextStyle.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "АВТОПРОХОД // BYE",
                style = HackMonoTextStyle.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            )
        }

        Text(
            text = "—",
            style = HackMonoTextStyle.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        )
    }
}
