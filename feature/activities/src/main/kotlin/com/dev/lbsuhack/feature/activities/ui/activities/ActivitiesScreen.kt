package com.dev.lbsuhack.feature.activities.ui.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dev.lbsuhack.core.localization.text.resolve
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.presentation.activities.ActivitiesViewModel
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import com.dev.lbsuhack.feature.activities.ui.component.ActivityRating
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVoteButton
import com.dev.lbsuhack.feature.activities.ui.component.ActivityVotersSummary
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActivitiesScreen(
    onOpenDetails: (activityId: String) -> Unit,
    onOpenCreate: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: ActivitiesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val retry = stringResource(R.string.activities_retry)
    LaunchedEffect(state.error) {
        state.error?.let {
            val result = snackbar.showSnackbar(it.resolve(context), actionLabel = retry)
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.accept(ActivitiesStore.Intent.Refresh)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                is ActivitiesStore.Label.OpenDetails -> onOpenDetails(label.activityId)
                ActivitiesStore.Label.OpenCreate -> onOpenCreate()
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                state.filteredActivities.isEmpty() -> {
                    ActivitiesEmpty(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            items = state.filteredActivities,
                            key = { it.id },
                        ) { proposal ->
                            ActivityCard(
                                proposal = proposal,
                                isAuthor = proposal.isCreatedBy(state.currentUser?.id, state.currentUser?.schoolsById),
                                onClick = {
                                    viewModel.accept(ActivitiesStore.Intent.ClickActivity(proposal.id))
                                },
                                canInteract = state.currentUser?.role == UserRole.PUPIL,
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
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActivityScanButton(
                    onOpenDetails = onOpenDetails,
                    modifier = Modifier.weight(1f).height(72.dp),
                )
                Button(
                    modifier = Modifier.weight(1f).height(72.dp),
                    onClick = { viewModel.accept(ActivitiesStore.Intent.ClickCreate) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(if (state.currentUser?.role == UserRole.PUPIL) R.string.activities_create_action else R.string.activities_review_action),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivitiesTopBar(
    userName: String?,
    pupilClass: String?,
    selectedCategory: ActivityCategory?,
    selectedTab: ActivityTab,
    onSelectCategory: (ActivityCategory?) -> Unit,
    onSelectTab: (ActivityTab) -> Unit,
    onOpenProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(top = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.activities_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (userName != null) {
                        stringResource(
                            R.string.activities_greeting_format,
                            userName,
                            pupilClass ?: "10-А",
                        )
                    } else {
                        stringResource(R.string.activities_subtitle)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            ProfileAvatar(name = userName, onOpenProfile = onOpenProfile)
        }

        Spacer(modifier = Modifier.height(20.dp))

        ActivitiesFilters(
            selectedCategory = selectedCategory,
            selectedTab = selectedTab,
            onSelectCategory = onSelectCategory,
            onSelectTab = onSelectTab,
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = MaterialTheme.hack.gridLine)
    }
}

@Composable
private fun ProfileAvatar(
    name: String?,
    onOpenProfile: () -> Unit,
) {
    val initial = name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onOpenProfile),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun ActivityCard(
    proposal: ActivityProposal,
    isAuthor: Boolean,
    canInteract: Boolean,
    onClick: () -> Unit,
    onVote: () -> Unit,
    onUnvote: () -> Unit,
    onLike: () -> Unit,
    onUnlike: () -> Unit,
) {
    val progress = proposal.progress

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
            .clickable(onClick = onClick),
    ) {
        proposal.photoUri?.let { photoUri ->
            ActivityCardCover(proposal = proposal, photoUri = photoUri, isAuthor = isAuthor)
        }
        Column(modifier = Modifier.padding(20.dp)) {
            if (proposal.photoUri == null) {
                ActivityMetadata(proposal = proposal, isAuthor = isAuthor)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = proposal.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = listOf(proposal.authorName, proposal.authorClass).filter(String::isNotBlank).joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = proposal.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            if (proposal.status == ActivityStatus.FINALIZED &&
                (proposal.confirmedTime != null || proposal.confirmedLocation != null)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ConfirmedActivityInfo(proposal = proposal)
            }

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
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = when {
                            proposal.isQuorumReached -> stringResource(R.string.activities_quorum_reached)
                            else -> stringResource(
                                R.string.activities_min_max_format,
                                proposal.voteThreshold,
                            )
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(12.dp))

                ActivityVotersSummary(voters = proposal.voters)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActivityRating(
                    likesCount = proposal.likesCount,
                    likedByMe = proposal.likedByMe,
                    enabled = canInteract && proposal.canLike,
                    onLike = onLike,
                    onUnlike = onUnlike,
                )
                if (canInteract) {
                    ActivityVoteButton(
                        proposal = proposal,
                        isAuthor = isAuthor,
                        onVote = onVote,
                        onUnvote = onUnvote,
                        onOpenDetails = onClick,
                        modifier = Modifier.weight(1f).height(50.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmedActivityInfo(proposal: ActivityProposal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.shapes.large,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        proposal.confirmedTime?.let { time ->
            ConfirmedInfoRow(icon = Icons.Outlined.Schedule, text = time)
        }
        proposal.confirmedLocation?.let { location ->
            ConfirmedInfoRow(icon = Icons.Outlined.LocationOn, text = location)
        }
    }
}

@Composable
private fun ConfirmedInfoRow(
    icon: ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ActivitiesEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.activities_empty_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.activities_empty_desc),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal fun ActivityCategory.icon(): ImageVector = when (this) {
    ActivityCategory.SPORTS -> Icons.Outlined.SportsBasketball
    ActivityCategory.INTELLECTUAL -> Icons.Outlined.Casino
    ActivityCategory.CULTURAL -> Icons.Outlined.Museum
    ActivityCategory.CREATIVE -> Icons.Outlined.Palette
    ActivityCategory.EDUCATIONAL -> Icons.Outlined.Computer
    ActivityCategory.VOLUNTEER -> Icons.Outlined.AutoAwesome
    ActivityCategory.ENVIRONMENTAL -> Icons.Outlined.CalendarMonth
    ActivityCategory.TOURISM -> Icons.Outlined.LocationOn
    ActivityCategory.ENTERTAINMENT -> Icons.Outlined.Movie
    ActivityCategory.OTHER -> Icons.Outlined.AutoAwesome
}

internal fun ActivityCategory.labelRes(): Int = when (this) {
    ActivityCategory.SPORTS -> R.string.activities_filter_sports
    ActivityCategory.INTELLECTUAL -> R.string.activities_filter_intellectual
    ActivityCategory.CULTURAL -> R.string.activities_filter_cultural
    ActivityCategory.CREATIVE -> R.string.activities_filter_creative
    ActivityCategory.EDUCATIONAL -> R.string.activities_filter_educational
    ActivityCategory.VOLUNTEER -> R.string.activities_filter_volunteer
    ActivityCategory.ENVIRONMENTAL -> R.string.activities_filter_environmental
    ActivityCategory.TOURISM -> R.string.activities_filter_tourism
    ActivityCategory.ENTERTAINMENT -> R.string.activities_filter_entertainment
    ActivityCategory.OTHER -> R.string.activities_filter_other
}

internal fun ActivityStatus.labelRes(): Int = when (this) {
    ActivityStatus.ON_MODERATION -> R.string.activities_status_moderating
    ActivityStatus.ON_VOTE -> R.string.activities_status_recruiting
    ActivityStatus.ON_ADMIN_MODERATION -> R.string.activities_status_pending_admin
    ActivityStatus.FINALIZED -> R.string.activities_status_approved
    ActivityStatus.DECLINED,
    ActivityStatus.DECLINED_BY_ADMIN,
    ActivityStatus.EXPIRED -> R.string.activities_status_rejected
}
