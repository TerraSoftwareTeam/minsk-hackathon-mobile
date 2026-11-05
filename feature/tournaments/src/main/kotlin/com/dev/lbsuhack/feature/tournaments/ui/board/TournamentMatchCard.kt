package com.dev.lbsuhack.feature.tournaments.ui.board

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        match.status == MatchStatus.LIVE -> MaterialTheme.hack.glow
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (isSelected || match.status == MatchStatus.LIVE) 2.dp else 1.dp

    Card(
        modifier = modifier
            .size(width = 240.dp, height = 110.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            MatchHeader(status = match.status)

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
private fun MatchHeader(status: MatchStatus) {
    val (label, containerColor, contentColor) = when (status) {
        MatchStatus.LIVE -> Triple(
            "LIVE • Идёт сейчас",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        )
        MatchStatus.BYE -> Triple(
            "BYE • Автопроход",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
        )
        MatchStatus.COMPLETED -> Triple(
            "Завершён",
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MatchStatus.SCHEDULED -> Triple(
            "Ожидание",
            MaterialTheme.colorScheme.surfaceContainerLow,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor,
    ) {
        Text(
            text = label,
            style = HackMonoTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isWinner) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = team.name.take(2).uppercase(),
                    style = HackMonoTextStyle.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (isWinner) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
                    color = if (isWinner) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (team.subtitle != null) {
                    Text(
                        text = team.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        val scoreText = team.score?.toString() ?: "-"
        val scoreColor = when {
            isWinner -> MaterialTheme.hack.success
            isLive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Text(
            text = scoreText,
            style = HackMonoTextStyle.copy(
                fontSize = 13.sp,
                fontWeight = if (isWinner || isLive) FontWeight.Bold else FontWeight.Normal,
            ),
            color = scoreColor,
        )
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
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "—",
                    style = HackMonoTextStyle.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Автопроход (BYE)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            )
        }

        Text(
            text = "—",
            style = HackMonoTextStyle.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
    }
}
