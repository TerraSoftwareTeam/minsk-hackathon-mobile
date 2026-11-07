package com.dev.lbsuhack.feature.activities.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.ui.component.HackSnackbarHost
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.core.ui.component.showHackSnackbar
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.presentation.actionsFor
import com.dev.lbsuhack.feature.activities.presentation.details.ActivityDetailsViewModel
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStore
import com.dev.lbsuhack.feature.activities.ui.details.component.ActivityHeroCard
import com.dev.lbsuhack.feature.activities.ui.details.component.ActivityStatusNotice
import com.dev.lbsuhack.feature.activities.ui.details.component.DeleteActivityDialog
import com.dev.lbsuhack.feature.activities.ui.details.component.DetailsFloatingActions
import com.dev.lbsuhack.feature.activities.ui.details.component.DetailsStateView
import com.dev.lbsuhack.feature.activities.ui.details.component.ParticipantsCard
import com.dev.lbsuhack.feature.activities.ui.details.component.StatusTimelineCard
import com.dev.lbsuhack.feature.activities.ui.details.component.SuccessChanceCard
import com.dev.lbsuhack.feature.activities.ui.details.component.VotingStatusCard
import com.dev.lbsuhack.feature.activities.ui.details.dialog.ActivityShareSheet
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ActivityDetailsScreen(
    activityId: String,
    onBack: () -> Unit,
    viewModel: ActivityDetailsViewModel = koinViewModel(parameters = { parametersOf(activityId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val retry = stringResource(R.string.activities_retry)
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showShareSheet by rememberSaveable(activityId) { mutableStateOf(false) }
    val shareActivity = state.activity

    if (showShareSheet && shareActivity?.canShare == true) {
        ActivityShareSheet(
            activityId = shareActivity.id,
            title = shareActivity.title,
            onDismiss = { showShareSheet = false },
        )
    }

    if (showDeleteDialog && state.canDelete) {
        DeleteActivityDialog(
            isDeleting = state.isDeleting,
            onConfirm = { viewModel.accept(ActivityDetailsStore.Intent.Delete) },
            onDismiss = { showDeleteDialog = false },
        )
    }

    LaunchedEffect(state.errorId) {
        state.error?.let {
            val result = snackbar.showHackSnackbar(it.resolve(context), actionLabel = retry)
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.accept(ActivityDetailsStore.Intent.Load(activityId))
            }
        }
    }

    LaunchedEffect(activityId) {
        viewModel.accept(ActivityDetailsStore.Intent.Load(activityId))
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.accept(ActivityDetailsStore.Intent.StartPolling)
        onPauseOrDispose { viewModel.accept(ActivityDetailsStore.Intent.StopPolling) }
    }

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                ActivityDetailsStore.Label.Back -> onBack()
                ActivityDetailsStore.Label.Deleted -> {
                    showDeleteDialog = false
                    onBack()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            val actions = state.activity?.actionsFor(state.currentUser?.role)
            HackSnackbarHost(
                snackbar,
                modifier = Modifier.padding(bottom = if (actions?.isVisible == true) 88.dp else 0.dp),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HackTopBar(
                title = stringResource(R.string.details_title),
                onBack = { viewModel.accept(ActivityDetailsStore.Intent.Back) },
                actions = {
                    if (state.activity?.canShare == true) {
                        IconButton(onClick = { showShareSheet = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.activity_share_action),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    if (state.canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.details_delete_action),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        val proposal = state.activity
        if (proposal == null) {
            DetailsStateView(
                loadState = state.loadState,
                onRetry = { viewModel.accept(ActivityDetailsStore.Intent.Load(activityId)) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            val actions = proposal.actionsFor(state.currentUser?.role)
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ActivityHeroCard(proposal = proposal, isAuthor = state.isAuthor)
                    ActivityStatusNotice(proposal = proposal)
                    StatusTimelineCard(status = proposal.status, origin = proposal.origin)
                    if (proposal.status == ActivityStatus.ON_VOTE ||
                        proposal.status == ActivityStatus.ON_ADMIN_MODERATION
                    ) {
                        VotingStatusCard(proposal = proposal)
                    }
                    val probability = proposal.successProbability
                    if (proposal.showsChance && probability != null) {
                        SuccessChanceCard(probability = probability)
                    }
                    ParticipantsCard(voters = proposal.voters, viewerRole = state.currentUser?.role)
                    if (actions.isVisible) {
                        Spacer(modifier = Modifier.height(if (actions.showParticipation) 144.dp else 96.dp))
                    }
                }
                if (actions.isVisible) {
                    DetailsFloatingActions(
                        proposal = proposal,
                        viewerRole = state.currentUser?.role,
                        canInteract = state.canInteract,
                        canVote = state.canVote,
                        onVote = { viewModel.accept(ActivityDetailsStore.Intent.Vote) },
                        onUnvote = { viewModel.accept(ActivityDetailsStore.Intent.Unvote) },
                        onLike = { viewModel.accept(ActivityDetailsStore.Intent.Like) },
                        onDislike = { viewModel.accept(ActivityDetailsStore.Intent.Dislike) },
                        onUnlike = { viewModel.accept(ActivityDetailsStore.Intent.Unlike) },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}
