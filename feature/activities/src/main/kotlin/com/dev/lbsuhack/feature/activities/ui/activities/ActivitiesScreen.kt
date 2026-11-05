package com.dev.lbsuhack.feature.activities.ui.activities

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.HackMonoTextStyle
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
                text = { Text(stringResource(R.string.activities_create_action)) },
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
                    ActivitiesEmpty(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
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
            .padding(top = 10.dp),
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (userName != null) {
                        "Привет, $userName · ${pupilClass ?: "10-А"}"
                    } else {
                        stringResource(R.string.activities_subtitle)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ProfileAvatar(name = userName ?: "У", onOpenProfile = onOpenProfile)
        }

        Spacer(modifier = Modifier.height(14.dp))

        ActivitiesCategoryRow(
            selectedCategory = selectedCategory,
            onSelectCategory = onSelectCategory,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActivitiesTabRow(
            selectedTab = selectedTab,
            onSelectTab = onSelectTab,
        )

        HorizontalDivider(color = MaterialTheme.hack.gridLine, thickness = 1.dp)
    }
}

@Composable
private fun ProfileAvatar(name: String, onOpenProfile: () -> Unit) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "К"
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onOpenProfile),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun ActivitiesCategoryRow(
    selectedCategory: ActivityCategory?,
    onSelectCategory: (ActivityCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            CategoryChip(
                text = "✨ ${stringResource(R.string.activities_filter_all)}",
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
            )
        }
        items(ActivityCategory.entries) { category ->
            CategoryChip(
                text = "${category.emoji()} ${stringResource(category.labelRes())}",
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun ActivitiesTabRow(
    selectedTab: ActivityTab,
    onSelectTab: (ActivityTab) -> Unit,
) {
    ScrollableTabRow(
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
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${proposal.category.emoji()} ${stringResource(proposal.category.labelRes())}",
                style = HackMonoTextStyle.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.primary,
            )

            StatusBadge(status = proposal.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = proposal.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${stringResource(R.string.activities_organizer_prefix)} ${proposal.authorName} (${proposal.authorClass})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = proposal.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (proposal.status == ActivityStatus.APPROVED && proposal.confirmedTime != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.hack.success.copy(alpha = 0.12f),
                        MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.hack.success,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = proposal.confirmedTime,
                    style = HackMonoTextStyle.copy(fontSize = 11.sp),
                    color = MaterialTheme.hack.success,
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (proposal.confirmedLocation != null) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.hack.success,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = proposal.confirmedLocation,
                        style = HackMonoTextStyle.copy(fontSize = 11.sp),
                        color = MaterialTheme.hack.success,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.activities_participants_format,
                    proposal.currentParticipantsCount,
                    proposal.maxParticipants,
                    proposal.minParticipants,
                ),
                style = HackMonoTextStyle.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (proposal.hasQuorum) {
                Text(
                    text = "Кворум набран",
                    style = HackMonoTextStyle.copy(fontSize = 11.sp),
                    color = MaterialTheme.hack.success,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.extraSmall),
            color = if (proposal.status == ActivityStatus.APPROVED) MaterialTheme.hack.success else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isJoined) {
                OutlinedButton(
                    onClick = onToggleJoin,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.hack.success,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.activities_leave_action),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else if (proposal.status == ActivityStatus.RECRUITING && !proposal.isFull) {
                Button(
                    onClick = onToggleJoin,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(stringResource(R.string.activities_join_action))
                }
            } else if (proposal.isFull) {
                Text(
                    text = stringResource(R.string.activities_full_badge),
                    style = HackMonoTextStyle.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun StatusBadge(status: ActivityStatus) {
    val (bgColor, textColor, textRes) = when (status) {
        ActivityStatus.MODERATING -> Triple(
            Color(0xFF382F10),
            Color(0xFFFFC107),
            R.string.activities_status_moderating,
        )
        ActivityStatus.RECRUITING -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary,
            R.string.activities_status_recruiting,
        )
        ActivityStatus.PENDING_ADMIN -> Triple(
            Color(0xFF3E2310),
            Color(0xFFFF9800),
            R.string.activities_status_pending_admin,
        )
        ActivityStatus.APPROVED -> Triple(
            Color(0xFF103319),
            Color(0xFF4CAF50),
            R.string.activities_status_approved,
        )
        ActivityStatus.REJECTED -> Triple(
            Color(0xFF3B1515),
            Color(0xFFF44336),
            R.string.activities_status_rejected,
        )
    }

    Box(
        modifier = Modifier
            .background(bgColor, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(textRes),
            style = HackMonoTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = textColor,
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
        Text(
            text = "📅",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.activities_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.activities_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
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

internal fun ActivityCategory.emoji(): String = when (this) {
    ActivityCategory.CINEMA -> "🎬"
    ActivityCategory.BOARD_GAMES -> "🎲"
    ActivityCategory.SPORTS -> "⚽"
    ActivityCategory.CREATIVITY -> "🎨"
    ActivityCategory.EXCURSION -> "🏛️"
    ActivityCategory.SCIENCE -> "💻"
    ActivityCategory.OTHER -> "✨"
}

private fun ActivityTab.labelRes(): Int = when (this) {
    ActivityTab.ALL -> R.string.activities_tab_all
    ActivityTab.RECRUITING -> R.string.activities_tab_recruiting
    ActivityTab.PENDING_ADMIN -> R.string.activities_tab_pending_admin
    ActivityTab.APPROVED -> R.string.activities_tab_approved
    ActivityTab.MY_PROPOSALS -> R.string.activities_tab_my_proposals
    ActivityTab.MY_PARTICIPATIONS -> R.string.activities_tab_my_participations
}
