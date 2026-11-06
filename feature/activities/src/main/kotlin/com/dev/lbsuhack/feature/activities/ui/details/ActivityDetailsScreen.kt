package com.dev.lbsuhack.feature.activities.ui.details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.presentation.details.ActivityDetailsViewModel
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStore
import com.dev.lbsuhack.feature.activities.ui.activities.ActivityCardCover
import com.dev.lbsuhack.feature.activities.ui.activities.ActivityMetadata
import com.dev.lbsuhack.feature.activities.ui.activities.FloatingActionsBackdrop
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailsScreen(
    activityId: String,
    onBack: () -> Unit,
    viewModel: ActivityDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(activityId) {
        viewModel.accept(ActivityDetailsStore.Intent.Load(activityId))
    }

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                ActivityDetailsStore.Label.Back -> onBack()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.details_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.accept(ActivityDetailsStore.Intent.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        val proposal = state.activity
        if (state.isLoading && proposal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (proposal == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.activity_qr_not_found))
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ActivityHeroCard(proposal = proposal)
                    ActivityQrCard(activityId = proposal.id, title = proposal.title)
                    if (proposal.status != ActivityStatus.ON_VOTE) {
                        ActivityStatusNotice(proposal = proposal)
                    }
                    StatusTimelineCard(status = proposal.status)
                    VotingStatusCard(proposal = proposal)
                    Spacer(modifier = Modifier.height(144.dp))
                }
                DetailsFloatingActions(
                    proposal = proposal,
                    hasVoted = state.hasVoted,
                    isAuthor = state.isAuthor,
                    onVote = { viewModel.accept(ActivityDetailsStore.Intent.Vote) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun ActivityHeroCard(proposal: ActivityProposal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge,
            ),
    ) {
        proposal.photoUri?.let { photoUri ->
            ActivityCardCover(proposal = proposal, photoUri = photoUri)
        }
        Column(modifier = Modifier.padding(20.dp)) {
            if (proposal.photoUri == null) {
                ActivityMetadata(proposal = proposal)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${proposal.authorName}, ${proposal.authorClass}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (proposal.targetClass.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.activities_target_class_format, proposal.targetClass),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (proposal.teacherName != null && proposal.teacherName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = proposal.teacherName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.hack.gridLine)
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = proposal.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (proposal.preferenceNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            MaterialTheme.shapes.large,
                        )
                        .padding(14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.details_preferences_title),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = proposal.preferenceNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityStatusNotice(proposal: ActivityProposal) {
    val containerColor: Color
    val contentColor: Color
    val icon: ImageVector
    val titleRes: Int
    val descriptionRes: Int?

    when (proposal.status) {
        ActivityStatus.ON_MODERATION -> {
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            icon = Icons.Outlined.HourglassEmpty
            titleRes = R.string.activities_status_moderating
            descriptionRes = R.string.details_moderating_desc
        }
        ActivityStatus.ON_VOTE -> {
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            contentColor = MaterialTheme.colorScheme.primary
            icon = Icons.Outlined.Groups
            titleRes = R.string.details_recruiting_title
            descriptionRes = R.string.details_recruiting_desc
        }
        ActivityStatus.ON_ADMIN_MODERATION -> {
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            icon = Icons.Outlined.HourglassEmpty
            titleRes = R.string.details_admin_pending_title
            descriptionRes = R.string.details_admin_pending_desc
        }
        ActivityStatus.FINALIZED -> {
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            contentColor = MaterialTheme.colorScheme.primary
            icon = Icons.Default.CheckCircle
            titleRes = R.string.details_admin_approved_title
            descriptionRes = null
        }
        ActivityStatus.DECLINED,
        ActivityStatus.DECLINED_BY_ADMIN,
        ActivityStatus.EXPIRED -> {
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            contentColor = MaterialTheme.colorScheme.outline
            icon = Icons.Default.CheckCircle
            titleRes = R.string.details_rejected_title
            descriptionRes = if (proposal.declineReason.isNullOrBlank()) R.string.details_rejected_desc else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = contentColor.copy(alpha = 0.24f),
                shape = MaterialTheme.shapes.extraLarge,
            )
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (descriptionRes != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (proposal.declineReason != null && proposal.declineReason.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = proposal.declineReason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (proposal.status == ActivityStatus.FINALIZED &&
            (proposal.confirmedTime != null || proposal.confirmedLocation != null)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                proposal.confirmedTime?.let { time ->
                    ConfirmedDetailsRow(
                        icon = Icons.Outlined.Schedule,
                        label = stringResource(R.string.details_confirmed_time_label),
                        value = time,
                        accent = contentColor,
                    )
                }
                proposal.confirmedLocation?.let { location ->
                    ConfirmedDetailsRow(
                        icon = Icons.Outlined.LocationOn,
                        label = stringResource(R.string.details_confirmed_location_label),
                        value = location,
                        accent = contentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmedDetailsRow(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = accent,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun StatusTimelineCard(status: ActivityStatus) {
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
        modifier = Modifier
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

@Composable
private fun VotingStatusCard(proposal: ActivityProposal) {
    Column(
        modifier = Modifier
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.activities_capacity_format,
                    proposal.votesCount,
                    proposal.voteThreshold,
                ),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    R.string.activities_min_max_format,
                    proposal.voteThreshold,
                ),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { proposal.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (proposal.isQuorumReached) {
                stringResource(R.string.activities_quorum_reached)
            } else {
                stringResource(
                    R.string.activities_min_max_format,
                    proposal.voteThreshold,
                )
            },
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailsFloatingActions(
    proposal: ActivityProposal,
    hasVoted: Boolean,
    isAuthor: Boolean,
    onVote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canVote = proposal.status == ActivityStatus.ON_VOTE
    if (!isAuthor && !hasVoted && !canVote) return

    Box(modifier = modifier.fillMaxWidth().height(180.dp)) {
        FloatingActionsBackdrop(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (isAuthor) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = stringResource(R.string.details_author_badge),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            when {
                hasVoted -> {
                    OutlinedButton(
                        onClick = onVote,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.activities_leave_action),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
                canVote -> {
                    Button(
                        onClick = onVote,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = MaterialTheme.shapes.large,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.activities_join_action),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}
