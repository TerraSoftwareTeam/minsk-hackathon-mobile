package com.dev.lbsuhack.feature.activities.ui.details

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.presentation.details.ActivityDetailsViewModel
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStore
import com.dev.lbsuhack.feature.activities.ui.activities.CategoryBadge
import com.dev.lbsuhack.feature.activities.ui.activities.StatusBadge
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
        bottomBar = {
            state.activity?.let { proposal ->
                DetailsBottomBar(
                    proposal = proposal,
                    hasVoted = state.hasVoted,
                    onVote = { viewModel.accept(ActivityDetailsStore.Intent.Vote) },
                )
            }
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
                CircularProgressIndicator()
            }
        } else if (proposal != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ActivityHeroCard(proposal = proposal)
                ActivityStatusNotice(proposal = proposal)
                StatusTimelineCard(status = proposal.status)
                VoteProgressCard(proposal = proposal)
                Spacer(modifier = Modifier.height(12.dp))
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
            )
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryBadge(
                category = proposal.category,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(10.dp))
            StatusBadge(status = proposal.status)
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                text = "${proposal.authorName} (${proposal.authorClass})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

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
                text = stringResource(
                    R.string.activities_target_class_format,
                    when (proposal.scope) {
                        ActivityScope.CLASS -> stringResource(R.string.create_activity_scope_class)
                        ActivityScope.GRADE -> stringResource(R.string.create_activity_scope_grade)
                        ActivityScope.SCHOOL -> stringResource(R.string.create_activity_scope_school)
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (proposal.startsAt.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = proposal.startsAt,
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            icon = Icons.Outlined.HourglassEmpty
            titleRes = R.string.activities_status_moderating
            descriptionRes = R.string.details_moderating_desc
        }
        ActivityStatus.ON_VOTE -> {
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            contentColor = MaterialTheme.colorScheme.primary
            icon = Icons.Outlined.Groups
            titleRes = R.string.details_recruiting_title
            descriptionRes = R.string.details_recruiting_desc
        }
        ActivityStatus.ON_ADMIN_MODERATION -> {
            containerColor = MaterialTheme.hack.amber.container
            contentColor = MaterialTheme.hack.amber.accent
            icon = Icons.Outlined.HourglassEmpty
            titleRes = R.string.details_admin_pending_title
            descriptionRes = R.string.details_admin_pending_desc
        }
        ActivityStatus.FINALIZED -> {
            containerColor = MaterialTheme.hack.successContainer
            contentColor = MaterialTheme.hack.success
            icon = Icons.Default.CheckCircle
            titleRes = R.string.details_admin_approved_title
            descriptionRes = null
        }
        ActivityStatus.DECLINED,
        ActivityStatus.DECLINED_BY_ADMIN,
        ActivityStatus.EXPIRED -> {
            containerColor = MaterialTheme.hack.danger.copy(alpha = 0.12f)
            contentColor = MaterialTheme.hack.danger
            icon = Icons.Default.CheckCircle
            titleRes = R.string.details_rejected_title
            descriptionRes = R.string.details_rejected_desc
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
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
                            .background(
                                when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isCompleted && !isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(stepTitle),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
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
private fun VoteProgressCard(proposal: ActivityProposal) {
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
                text = if (proposal.isQuorumReached) {
                    stringResource(R.string.activities_quorum_reached)
                } else {
                    stringResource(R.string.activities_min_max_format, proposal.voteThreshold)
                },
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (proposal.isQuorumReached) MaterialTheme.hack.success else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { proposal.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun DetailsBottomBar(
    proposal: ActivityProposal,
    hasVoted: Boolean,
    onVote: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.hack.gridLine),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                    text = if (proposal.isQuorumReached) {
                        stringResource(R.string.activities_quorum_reached)
                    } else {
                        stringResource(R.string.activities_min_max_format, proposal.voteThreshold)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (proposal.isQuorumReached) MaterialTheme.hack.success else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when {
                hasVoted -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.activities_joined_badge),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }

                proposal.status == ActivityStatus.ON_VOTE -> {
                    Button(
                        onClick = onVote,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.activities_vote_action),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}
