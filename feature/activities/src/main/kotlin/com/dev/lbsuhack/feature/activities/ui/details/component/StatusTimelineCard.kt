package com.dev.lbsuhack.feature.activities.ui.details.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

@Composable
internal fun StatusTimelineCard(status: ActivityStatus, modifier: Modifier = Modifier) {
    val activeStep = when (status) {
        ActivityStatus.ON_MODERATION -> 1
        ActivityStatus.ON_VOTE -> 2
        ActivityStatus.ON_ADMIN_MODERATION -> 3
        ActivityStatus.FINALIZED -> 4
        ActivityStatus.DECLINED,
        ActivityStatus.DECLINED_BY_ADMIN,
        ActivityStatus.EXPIRED -> 0
    }
    val steps = listOf(
        R.string.details_step_created_short,
        R.string.details_step_moderated_short,
        R.string.details_step_recruiting_short,
        R.string.details_step_pending_short,
        R.string.details_step_approved_short,
    )

    val completedConnection = MaterialTheme.colorScheme.onSurface
    val pendingConnection = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge,
            )
            .padding(18.dp),
    ) {
        Text(
            text = stringResource(R.string.details_stage_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val stepWidth = size.width / steps.size
                    val radius = 15.dp.toPx()
                    val centerY = radius
                    for (index in 0 until steps.lastIndex) {
                        val start = stepWidth * (index + 0.5f) + radius
                        val end = stepWidth * (index + 1.5f) - radius
                        val isRtl = layoutDirection == LayoutDirection.Rtl
                        drawLine(
                            color = if (index < activeStep) completedConnection else pendingConnection,
                            start = Offset(if (isRtl) size.width - start else start, centerY),
                            end = Offset(if (isRtl) size.width - end else end, centerY),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                },
            verticalAlignment = Alignment.Top,
        ) {
            steps.forEachIndexed { index, stepTitle ->
                val isCompleted = index <= activeStep
                val isCurrent = index == activeStep

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .border(
                                width = 1.dp,
                                color = when {
                                    isCurrent -> MaterialTheme.colorScheme.onSurface
                                    isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isCompleted && !isCurrent) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val check = Path().apply {
                                    moveTo(size.width * 0.2f, size.height * 0.52f)
                                    lineTo(size.width * 0.42f, size.height * 0.74f)
                                    lineTo(size.width * 0.82f, size.height * 0.28f)
                                }
                                drawPath(
                                    path = check,
                                    color = completedConnection,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                                )
                            }
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isCurrent) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(stepTitle),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = if (isCurrent) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
