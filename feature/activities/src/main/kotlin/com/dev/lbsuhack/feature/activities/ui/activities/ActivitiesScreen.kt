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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.presentation.activities.ActivitiesViewModel
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActivitiesScreen(
    onOpenDetails: (activityId: String) -> Unit,
    onOpenCreate: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: ActivitiesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.accept(ActivitiesStore.Intent.Load)
        viewModel.labels.collect { label ->
            when (label) {
                is ActivitiesStore.Label.OpenDetails -> onOpenDetails(label.activityId)
                ActivitiesStore.Label.OpenCreate -> onOpenCreate()
                ActivitiesStore.Label.OpenProfile -> onOpenProfile()
            }
        }
    }

    Scaffold(
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.accept(ActivitiesStore.Intent.ClickCreate) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        text = stringResource(R.string.activities_create_action),
                        fontWeight = FontWeight.Bold,
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.filteredActivities.isEmpty() -> {
                    ActivitiesEmpty(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            items = state.filteredActivities,
                            key = { it.id },
                        ) { proposal ->
                            ActivityCard(
                                proposal = proposal,
                                currentUserId = state.currentUser?.id,
                                onClick = {
                                    viewModel.accept(ActivitiesStore.Intent.ClickActivity(proposal.id))
                                },
                                onToggleJoin = {
                                    viewModel.accept(ActivitiesStore.Intent.ToggleJoin(proposal.id))
                                },
                            )
                        }
                    }
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
                    style = MaterialTheme.typography.headlineLarge,
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

        ActivitiesCategoryRow(
            selectedCategory = selectedCategory,
            onSelectCategory = onSelectCategory,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ActivitiesTabRow(
            selectedTab = selectedTab,
            onSelectTab = onSelectTab,
        )

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
private fun ActivitiesCategoryRow(
    selectedCategory: ActivityCategory?,
    onSelectCategory: (ActivityCategory?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            CategoryChip(
                text = stringResource(R.string.activities_filter_all),
                icon = Icons.Outlined.AutoAwesome,
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
            )
        }
        items(ActivityCategory.entries) { category ->
            CategoryChip(
                text = stringResource(category.labelRes()),
                icon = category.icon(),
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .clip(shape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainer,
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivitiesTabRow(
    selectedTab: ActivityTab,
    onSelectTab: (ActivityTab) -> Unit,
) {
    SecondaryScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 20.dp,
        divider = {},
    ) {
        ActivityTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                text = {
                    Text(
                        text = stringResource(tab.labelRes()),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (selectedTab == tab) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
}

@Composable
private fun ActivityCard(
    proposal: ActivityProposal,
    currentUserId: String?,
    onClick: () -> Unit,
    onToggleJoin: () -> Unit,
) {
    val isJoined = currentUserId != null && proposal.isJoined(currentUserId)
    val progress = if (proposal.maxParticipants > 0) {
        (proposal.currentParticipantsCount.toFloat() / proposal.maxParticipants.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

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
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CategoryBadge(
                category = proposal.category,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(10.dp))
            StatusBadge(status = proposal.status)
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                text = "${proposal.authorName} · ${proposal.authorClass}",
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

        if (proposal.status == ActivityStatus.APPROVED &&
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
                        proposal.currentParticipantsCount,
                        proposal.maxParticipants,
                    ),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when {
                        proposal.isFull -> stringResource(R.string.activities_full_badge)
                        proposal.hasQuorum -> stringResource(R.string.activities_quorum_reached)
                        else -> stringResource(
                            R.string.activities_min_max_format,
                            proposal.minParticipants,
                            proposal.maxParticipants,
                        )
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (proposal.hasQuorum) MaterialTheme.hack.success
                    else MaterialTheme.colorScheme.onSurfaceVariant,
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isJoined -> {
                OutlinedButton(
                    onClick = onToggleJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.activities_leave_action),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            proposal.status == ActivityStatus.RECRUITING && !proposal.isFull -> {
                Button(
                    onClick = onToggleJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.activities_join_action),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            else -> {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = stringResource(R.string.activities_open_details),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
                MaterialTheme.hack.successContainer,
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
            tint = MaterialTheme.hack.success,
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
internal fun CategoryBadge(
    category: ActivityCategory,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    MaterialTheme.shapes.medium,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = category.icon(),
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = stringResource(category.labelRes()),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun StatusBadge(status: ActivityStatus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(statusColor(status), CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(
                when (status) {
                    ActivityStatus.MODERATING -> R.string.activities_status_moderating
                    ActivityStatus.RECRUITING -> R.string.activities_status_recruiting
                    ActivityStatus.PENDING_ADMIN -> R.string.activities_status_pending_admin
                    ActivityStatus.APPROVED -> R.string.activities_status_approved
                    ActivityStatus.REJECTED -> R.string.activities_status_rejected
                },
            ),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun statusColor(status: ActivityStatus): Color = when (status) {
    ActivityStatus.MODERATING -> MaterialTheme.colorScheme.onSurfaceVariant
    ActivityStatus.RECRUITING -> MaterialTheme.colorScheme.primary
    ActivityStatus.PENDING_ADMIN -> MaterialTheme.hack.amber.accent
    ActivityStatus.APPROVED -> MaterialTheme.hack.success
    ActivityStatus.REJECTED -> MaterialTheme.hack.danger
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
                tint = MaterialTheme.colorScheme.primary,
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal fun ActivityCategory.icon(): ImageVector = when (this) {
    ActivityCategory.CINEMA -> Icons.Outlined.Movie
    ActivityCategory.BOARD_GAMES -> Icons.Outlined.Casino
    ActivityCategory.SPORTS -> Icons.Outlined.SportsBasketball
    ActivityCategory.CREATIVITY -> Icons.Outlined.Palette
    ActivityCategory.EXCURSION -> Icons.Outlined.Museum
    ActivityCategory.SCIENCE -> Icons.Outlined.Computer
    ActivityCategory.OTHER -> Icons.Outlined.AutoAwesome
}

internal fun ActivityCategory.labelRes(): Int = when (this) {
    ActivityCategory.CINEMA -> R.string.activities_filter_cinema
    ActivityCategory.BOARD_GAMES -> R.string.activities_filter_board_games
    ActivityCategory.SPORTS -> R.string.activities_filter_sports
    ActivityCategory.CREATIVITY -> R.string.activities_filter_creativity
    ActivityCategory.EXCURSION -> R.string.activities_filter_excursion
    ActivityCategory.SCIENCE -> R.string.activities_filter_science
    ActivityCategory.OTHER -> R.string.activities_filter_other
}

private fun ActivityTab.labelRes(): Int = when (this) {
    ActivityTab.ALL -> R.string.activities_tab_all
    ActivityTab.RECRUITING -> R.string.activities_tab_recruiting
    ActivityTab.PENDING_ADMIN -> R.string.activities_tab_pending_admin
    ActivityTab.APPROVED -> R.string.activities_tab_approved
    ActivityTab.MY_PROPOSALS -> R.string.activities_tab_my_proposals
    ActivityTab.MY_PARTICIPATIONS -> R.string.activities_tab_my_participations
}
