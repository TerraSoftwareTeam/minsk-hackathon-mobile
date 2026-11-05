package com.dev.lbsuhack.feature.tournaments.ui.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.lbsuhack.core.designsystem.theme.HackMonoTextStyle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.tournaments.domain.StageStatus
import com.dev.lbsuhack.feature.tournaments.domain.TournamentBracket
import com.dev.lbsuhack.feature.tournaments.domain.TournamentStage

@Composable
internal fun TournamentStageSelector(
    currentStage: TournamentStage,
    bracket: TournamentBracket?,
    onSelectStage: (TournamentStage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (bracket?.status == StageStatus.ACTIVE) MaterialTheme.hack.success
                                else MaterialTheme.colorScheme.outline,
                            ),
                    )
                    Text(
                        text = if (bracket?.status == StageStatus.ACTIVE) "ТЕКУЩИЙ ЭТАП" else "СЛЕДУЮЩИЙ ЭТАП",
                        style = HackMonoTextStyle.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = if (bracket?.status == StageStatus.ACTIVE) MaterialTheme.hack.success
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "${bracket?.teamsCount ?: 0} команд в сетке",
                    style = HackMonoTextStyle.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = MaterialTheme.shapes.small,
                    )
                    .border(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                StageTabItem(
                    title = "1. Школа",
                    subtitle = "7 ком.",
                    isSelected = currentStage == TournamentStage.SCHOOL,
                    onClick = { onSelectStage(TournamentStage.SCHOOL) },
                    modifier = Modifier.weight(1f),
                )
                StageTabItem(
                    title = "2. Район",
                    subtitle = "5 школ",
                    isSelected = currentStage == TournamentStage.DISTRICT,
                    onClick = { onSelectStage(TournamentStage.DISTRICT) },
                    modifier = Modifier.weight(1f),
                )
                StageTabItem(
                    title = "3. Город",
                    subtitle = "9 р-нов",
                    isSelected = currentStage == TournamentStage.CITY,
                    onClick = { onSelectStage(TournamentStage.CITY) },
                    modifier = Modifier.weight(1f),
                )
            }

            if (bracket != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = bracket.stageName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${bracket.stageSubtitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StageTabItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = HackMonoTextStyle.copy(fontSize = 10.sp),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
