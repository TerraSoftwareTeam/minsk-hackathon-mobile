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
import com.dev.lbsuhack.feature.activities.domain.ActivityOrigin
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus

@Composable
internal fun StatusTimelineCard(
    status: ActivityStatus,
    origin: ActivityOrigin,
    modifier: Modifier = Modifier,
) {
    val steps = timelineSteps(origin)
    val activeStep = steps.indexOfFirst { status in it.statuses }.coerceAtLeast(0)
        .let { index -> if (status.isTerminalFailure()) 0 else index }
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
            steps.forEachIndexed { index, step ->
                val stepTitle = step.titleRes
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

private data class TimelineStep(val titleRes: Int, val statuses: Set<ActivityStatus>)

private fun timelineSteps(origin: ActivityOrigin): List<TimelineStep> {
    val created = TimelineStep(R.string.details_step_created_short, emptySet())
    val teacher = TimelineStep(R.string.details_step_moderated_short, setOf(ActivityStatus.ON_MODERATION))
    val votes = TimelineStep(R.string.details_step_recruiting_short, setOf(ActivityStatus.ON_VOTE))
    val school = TimelineStep(R.string.details_step_pending_short, setOf(ActivityStatus.ON_ADMIN_MODERATION))
    val done = TimelineStep(R.string.details_step_approved_short, setOf(ActivityStatus.FINALIZED))
    return when (origin) {
        ActivityOrigin.PUPIL -> listOf(created, teacher, votes, school, done)
        ActivityOrigin.TEACHER -> listOf(created, votes, school, done)
        ActivityOrigin.SCHOOL -> listOf(created, votes, done)
    }
}

private fun ActivityStatus.isTerminalFailure(): Boolean =
    this == ActivityStatus.DECLINED || this == ActivityStatus.DECLINED_BY_ADMIN || this == ActivityStatus.EXPIRED
