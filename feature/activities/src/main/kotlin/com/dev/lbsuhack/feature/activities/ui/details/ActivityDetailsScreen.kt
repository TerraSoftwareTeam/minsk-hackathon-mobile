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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.presentation.details.ActivityDetailsViewModel
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStore
import com.dev.lbsuhack.feature.activities.ui.details.component.ActivityHeroCard
import com.dev.lbsuhack.feature.activities.ui.details.component.ActivityQrCard
import com.dev.lbsuhack.feature.activities.ui.details.component.ActivityStatusNotice
import com.dev.lbsuhack.feature.activities.ui.details.component.DetailsFloatingActions
import com.dev.lbsuhack.feature.activities.ui.details.component.DetailsStateView
import com.dev.lbsuhack.feature.activities.ui.details.component.ParticipantsCard
import com.dev.lbsuhack.feature.activities.ui.details.component.StatusTimelineCard
import com.dev.lbsuhack.feature.activities.ui.details.component.SuccessChanceCard
import com.dev.lbsuhack.feature.activities.ui.details.component.VotingStatusCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActivityDetailsScreen(
    activityId: String,
    onBack: () -> Unit,
    viewModel: ActivityDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val retry = stringResource(R.string.activities_retry)

    LaunchedEffect(state.errorId) {
        state.error?.let {
            val result = snackbar.showSnackbar(it.resolve(context), actionLabel = retry)
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
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HackTopBar(
                title = stringResource(R.string.details_title),
                onBack = { viewModel.accept(ActivityDetailsStore.Intent.Back) },
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
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ActivityHeroCard(proposal = proposal, isAuthor = state.isAuthor)
                    if (proposal.canShare) {
                        ActivityQrCard(activityId = proposal.id, title = proposal.title)
                    }
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
                    ParticipantsCard(voters = proposal.voters)
                    Spacer(modifier = Modifier.height(144.dp))
                }
                DetailsFloatingActions(
                    proposal = proposal,
                    isAuthor = state.isAuthor,
                    canInteract = state.canInteract,
                    onVote = { viewModel.accept(ActivityDetailsStore.Intent.Vote) },
                    onUnvote = { viewModel.accept(ActivityDetailsStore.Intent.Unvote) },
                    onLike = { viewModel.accept(ActivityDetailsStore.Intent.Like) },
                    onUnlike = { viewModel.accept(ActivityDetailsStore.Intent.Unlike) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
