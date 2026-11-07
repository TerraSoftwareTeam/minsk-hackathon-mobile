package com.dev.lbsuhack.feature.activities.ui.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.ui.component.HackEmptyState
import com.dev.lbsuhack.core.ui.component.HackFloatingActionButton
import com.dev.lbsuhack.core.ui.component.HackLoading
import com.dev.lbsuhack.core.ui.component.HackSnackbarHost
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.core.ui.component.showHackSnackbar
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.presentation.activities.ActivitiesViewModel
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivitiesBottomActions
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivitiesSectionHeader
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivitiesTopBar
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivityCard
import com.dev.lbsuhack.feature.activities.ui.common.FloatingActionsBackdrop
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    onOpenDetails: (activityId: String) -> Unit,
    onOpenCreate: () -> Unit,
    onOpenRequests: (UserRole) -> Unit,
    onOpenProfile: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ActivitiesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var appliedFilters by remember { mutableStateOf(state.selectedCategory to state.selectedTab) }

    LaunchedEffect(state.selectedCategory, state.selectedTab) {
        val filters = state.selectedCategory to state.selectedTab
        if (filters != appliedFilters) {
            listState.requestScrollToItem(0)
            appliedFilters = filters
        }
    }

    var knownActivityIds by remember { mutableStateOf<Set<String>?>(null) }

    LaunchedEffect(state.activities, state.hasLoaded) {
        if (!state.hasLoaded) return@LaunchedEffect
        val ids = state.activities.mapTo(HashSet()) { it.id }
        val known = knownActivityIds
        knownActivityIds = ids
        if (known == null) return@LaunchedEffect
        val newIds = ids - known
        if (newIds.isEmpty()) return@LaunchedEffect
        state.indexOfFirstNew(newIds)?.let { index -> listState.animateScrollToItem(index) }
    }

    val staffRole = state.currentUser?.role?.takeIf { it != UserRole.PUPIL }
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val retry = stringResource(R.string.activities_retry)
    var scanError by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(state.errorId) {
        if (state.activities.isEmpty()) return@LaunchedEffect
        state.error?.let {
            val result = snackbar.showHackSnackbar(it.resolve(context), actionLabel = retry)
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.accept(ActivitiesStore.Intent.Refresh)
            }
        }
    }

    LaunchedEffect(scanError) {
        scanError?.let {
            snackbar.showHackSnackbar(it.resolve(context))
            scanError = null
        }
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
        snackbarHost = { HackSnackbarHost(snackbar, modifier = Modifier.padding(bottom = 88.dp)) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (staffRole != null) {
                HackTopBar(
                    title = stringResource(
                        if (staffRole == UserRole.ORGANIZATOR) {
                            R.string.activities_staff_title_school
                        } else {
                            R.string.activities_staff_title_teacher
                        },
                    ),
                    onBack = onBack,
                    backContentDescription = stringResource(R.string.activities_back),
                )
            } else {
                ActivitiesTopBar(
                    userName = state.currentUser?.name,
                    pupilClass = state.currentUser?.pupilClass,
                    selectedCategory = state.selectedCategory,
                    selectedTab = state.selectedTab,
                    onSelectCategory = { viewModel.accept(ActivitiesStore.Intent.SelectCategory(it)) },
                    onSelectTab = { viewModel.accept(ActivitiesStore.Intent.SelectTab(it)) },
                    onOpenProfile = { viewModel.accept(ActivitiesStore.Intent.ClickProfile) },
                    showQuickFilters = state.canInteract,
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PullToRefreshBox(
                isRefreshing = state.isLoading && state.activities.isNotEmpty(),
                onRefresh = { viewModel.accept(ActivitiesStore.Intent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isInitialLoading -> {
                        HackLoading()
                    }

                    state.activities.isEmpty() && state.error != null -> {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            item(key = LOAD_ERROR_KEY) {
                                HackEmptyState(
                                    title = stringResource(R.string.activities_load_error_title),
                                    description = state.error?.resolve(context),
                                    icon = Icons.Outlined.CloudOff,
                                    modifier = Modifier.fillParentMaxSize(),
                                    action = {
                                        Button(
                                            onClick = { viewModel.accept(ActivitiesStore.Intent.Refresh) },
                                            shape = MaterialTheme.shapes.medium,
                                        ) {
                                            Text(text = retry, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                )
                            }
                        }
                    }

                    state.filteredActivities.isEmpty() -> {
                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            item(key = EMPTY_STATE_KEY) {
                                if (state.hasActiveFilters) {
                                    HackEmptyState(
                                        title = stringResource(R.string.activities_filtered_empty_title),
                                        description = stringResource(R.string.activities_filtered_empty_desc),
                                        icon = Icons.Outlined.FilterAltOff,
                                        modifier = Modifier.fillParentMaxSize(),
                                        action = {
                                            OutlinedButton(
                                                onClick = { viewModel.accept(ActivitiesStore.Intent.ResetFilters) },
                                                shape = MaterialTheme.shapes.medium,
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.activities_filters_reset),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        },
                                    )
                                } else {
                                    HackEmptyState(
                                        title = stringResource(
                                            if (state.canInteract) R.string.activities_empty_title else R.string.activities_staff_empty_title,
                                        ),
                                        description = stringResource(
                                            if (state.canInteract) R.string.activities_empty_desc else R.string.activities_staff_empty_desc,
                                        ),
                                        icon = Icons.Outlined.CalendarMonth,
                                        modifier = Modifier.fillParentMaxSize(),
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 112.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                        ) {
                            val sections = state.feedSections
                            val showHeaders = sections.size > 1
                            sections.forEach { section ->
                                if (showHeaders) {
                                    item(key = "section-${section.stage.name}") {
                                        ActivitiesSectionHeader(
                                            stage = section.stage,
                                            count = section.activities.size,
                                            modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                        )
                                    }
                                }
                                items(
                                    items = section.activities,
                                    key = { it.id },
                                ) { proposal ->
                                    ActivityCard(
                                        proposal = proposal,
                                        isAuthor = state.isAuthorOf(proposal),
                                        viewerRole = state.currentUser?.role,
                                        onClick = {
                                            viewModel.accept(ActivitiesStore.Intent.ClickActivity(proposal.id))
                                        },
                                        canInteract = state.canInteract,
                                        canLike = state.canLikeIn(proposal),
                                        onVote = { viewModel.accept(ActivitiesStore.Intent.Vote(proposal.id)) },
                                        onUnvote = { viewModel.accept(ActivitiesStore.Intent.Unvote(proposal.id)) },
                                        onLike = { viewModel.accept(ActivitiesStore.Intent.Like(proposal.id)) },
                                        onDislike = { viewModel.accept(ActivitiesStore.Intent.Dislike(proposal.id)) },
                                        onUnlike = { viewModel.accept(ActivitiesStore.Intent.Unlike(proposal.id)) },
                                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            FloatingActionsBackdrop(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(180.dp),
            )
            if (staffRole != null) {
                HackFloatingActionButton(
                    text = stringResource(R.string.activities_create_action),
                    icon = Icons.Default.Add,
                    onClick = { viewModel.accept(ActivitiesStore.Intent.ClickCreate) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(64.dp),
                )
            } else {
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
}

private const val EMPTY_STATE_KEY = "activities-empty"
private const val LOAD_ERROR_KEY = "activities-load-error"

/** Lazy-list index to scroll to so the first newly arrived activity is visible at the top (with its section header). */
private fun ActivitiesStore.State.indexOfFirstNew(newIds: Set<String>): Int? {
    val sections = feedSections
    val showHeaders = sections.size > 1
    var index = 0
    sections.forEach { section ->
        val sectionStart = index
        if (showHeaders) index++
        section.activities.forEachIndexed { position, proposal ->
            if (proposal.id in newIds) return if (position == 0) sectionStart else index
            index++
        }
    }
    return null
}
