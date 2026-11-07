package com.dev.lbsuhack.feature.activities.ui.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.presentation.activities.ActivitiesViewModel
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.core.ui.component.HackEmptyState
import com.dev.lbsuhack.core.ui.component.HackLoading
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivitiesBottomActions
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivitiesTopBar
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivityCard
import com.dev.lbsuhack.feature.activities.ui.common.FloatingActionsBackdrop
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActivitiesScreen(
    onOpenDetails: (activityId: String) -> Unit,
    onOpenCreate: () -> Unit,
    onOpenRequests: (UserRole) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: ActivitiesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val retry = stringResource(R.string.activities_retry)
    var scanError by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(state.errorId) {
        state.error?.let {
            val result = snackbar.showSnackbar(it.resolve(context), actionLabel = retry)
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.accept(ActivitiesStore.Intent.Refresh)
            }
        }
    }

    LaunchedEffect(scanError) {
        scanError?.let { snackbar.showSnackbar(it.resolve(context)) }
    }

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                is ActivitiesStore.Label.OpenDetails -> onOpenDetails(label.activityId)
                ActivitiesStore.Label.OpenCreate -> onOpenCreate()
                is ActivitiesStore.Label.OpenRequests -> onOpenRequests(label.role)
                ActivitiesStore.Label.OpenProfile -> onOpenProfile()
            }
        }
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.accept(ActivitiesStore.Intent.StartPolling)
        onPauseOrDispose { viewModel.accept(ActivitiesStore.Intent.StopPolling) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ActivitiesTopBar(
                userName = state.currentUser?.name,
                pupilClass = state.currentUser?.pupilClass,
                selectedCategory = state.selectedCategory,
                selectedTab = state.selectedTab,
                onSelectCategory = { viewModel.accept(ActivitiesStore.Intent.SelectCategory(it)) },
                onSelectTab = { viewModel.accept(ActivitiesStore.Intent.SelectTab(it)) },
                onOpenProfile = { viewModel.accept(ActivitiesStore.Intent.ClickProfile) },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading && state.activities.isEmpty() -> {
                    HackLoading()
                }

                state.filteredActivities.isEmpty() -> {
                    HackEmptyState(
                        title = stringResource(R.string.activities_empty_title),
                        description = stringResource(R.string.activities_empty_desc),
                        icon = Icons.Outlined.CalendarMonth,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 112.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            items = state.filteredActivities,
                            key = { it.id },
                        ) { proposal ->
                            ActivityCard(
                                proposal = proposal,
                                isAuthor = state.isAuthorOf(proposal),
                                onClick = {
                                    viewModel.accept(ActivitiesStore.Intent.ClickActivity(proposal.id))
                                },
                                canInteract = state.canInteract,
                                canLike = state.canLikeIn(proposal),
                                onVote = { viewModel.accept(ActivitiesStore.Intent.Vote(proposal.id)) },
                                onUnvote = { viewModel.accept(ActivitiesStore.Intent.Unvote(proposal.id)) },
                                onLike = { viewModel.accept(ActivitiesStore.Intent.Like(proposal.id)) },
                                onUnlike = { viewModel.accept(ActivitiesStore.Intent.Unlike(proposal.id)) },
                            )
                        }
                    }
                }
            }
            FloatingActionsBackdrop(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(180.dp),
            )
            ActivitiesBottomActions(
                actionLabel = state.primaryActionLabel.resolve(context),
                onOpenDetails = onOpenDetails,
                onScanError = { scanError = it },
                onPrimaryAction = { viewModel.accept(ActivitiesStore.Intent.ClickCreate) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
