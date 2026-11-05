package com.dev.lbsuhack.feature.feed.ui.feed

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.HackMonoTextStyle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.core.localization.text.asString
import com.dev.lbsuhack.feature.feed.R
import com.dev.lbsuhack.feature.feed.domain.EventCategory
import com.dev.lbsuhack.feature.feed.domain.FeedEvent
import com.dev.lbsuhack.feature.feed.presentation.feed.FeedViewModel
import com.dev.lbsuhack.feature.feed.store.feed.FeedStore
import org.koin.androidx.compose.koinViewModel

private const val CurrentUserInitials = "Н"

private val EventCardHeight = 176.dp
private val EventBadgeWidth = 56.dp
private val EventBadgeHeight = 76.dp

@Composable
fun FeedScreen(
    onOpenDetails: (eventId: String) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.accept(FeedStore.Intent.Load)
        viewModel.labels.collect { label ->
            when (label) {
                is FeedStore.Label.OpenDetails -> onOpenDetails(label.eventId)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FeedTopBar(
                eventCount = state.visibleEvents.size,
                selectedCategory = state.selectedCategory,
                onSelectCategory = { viewModel.accept(FeedStore.Intent.SelectCategory(it)) },
                onOpenProfile = onOpenProfile,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading && state.events.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null && state.events.isEmpty() -> {
                    FeedError(
                        message = state.error!!.asString(),
                        onRetry = { viewModel.accept(FeedStore.Intent.Refresh) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.visibleEvents.isEmpty() -> {
                    FeedEmpty(
                        isFiltered = state.events.isNotEmpty(),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.visibleEvents, key = FeedEvent::id) { event ->
                            FeedEventCard(
                                event = event,
                                expanded = event.id in state.expandedEventIds,
                                onToggleExpanded = {
                                    viewModel.accept(FeedStore.Intent.ToggleExpanded(event.id))
                                },
                                onOpenDetails = {
                                    viewModel.accept(FeedStore.Intent.ClickEvent(event.id))
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
private fun FeedTopBar(
    eventCount: Int,
    selectedCategory: EventCategory?,
    onSelectCategory: (EventCategory?) -> Unit,
    onOpenProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(top = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.feed_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pluralStringResource(R.plurals.feed_events_count, eventCount, eventCount),
                    style = HackMonoTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ProfileAvatar(onOpenProfile = onOpenProfile)
        }
        Spacer(modifier = Modifier.height(16.dp))
        FeedFilters(
            selectedCategory = selectedCategory,
            onSelectCategory = onSelectCategory,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.hack.gridLine)
    }
}

@Composable
private fun ProfileAvatar(onOpenProfile: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.hack.glow,
                spotColor = MaterialTheme.hack.glow,
            )
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                    ),
                ),
                shape = CircleShape,
            )
            .clickable(onClick = onOpenProfile),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = CurrentUserInitials,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun FeedFilters(
    selectedCategory: EventCategory?,
    onSelectCategory: (EventCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FeedFilterChip(
                text = stringResource(R.string.feed_filter_all),
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
            )
        }
        items(EventCategory.entries) { category ->
            FeedFilterChip(
                text = stringResource(category.labelRes()),
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
            )
        }
    }
}

@Composable
private fun FeedFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.outline
            },
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

internal fun EventCategory.labelRes(): Int = when (this) {
    EventCategory.HACKATHON -> R.string.feed_filter_hackathon
    EventCategory.MEETUP -> R.string.feed_filter_meetup
    EventCategory.WORKSHOP -> R.string.feed_filter_workshop
    EventCategory.LECTURE -> R.string.feed_filter_lecture
}

@Composable
private fun FeedEventCard(
    event: FeedEvent,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "eventCardChevron",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(EventCardHeight)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onToggleExpanded),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (expanded) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EventDateBadge(day = event.day, month = event.month)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(event.category.labelRes()).uppercase(),
                        style = HackMonoTextStyle.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(
                        targetState = expanded,
                        animationSpec = tween(durationMillis = 200),
                        label = "eventCardContent",
                    ) { isExpanded ->
                        if (isExpanded) {
                            Column {
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    event.tags.take(3).forEach { tag -> EventTag(text = tag) }
                                }
                            }
                        } else {
                            Text(
                                text = event.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                EventCardFooter(event = event, onOpenDetails = onOpenDetails)
            }
        }
    }
}

@Composable
private fun EventDateBadge(day: String, month: String) {
    Box(
        modifier = Modifier
            .size(width = EventBadgeWidth, height = EventBadgeHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = month.uppercase(),
                style = HackMonoTextStyle.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun EventTag(text: String) {
    Text(
        text = "#$text",
        style = HackMonoTextStyle.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
private fun EventCardFooter(
    event: FeedEvent,
    onOpenDetails: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = event.time,
            style = HackMonoTextStyle,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = event.place,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "${event.participants}/${event.capacity}",
            style = HackMonoTextStyle,
            color = if (event.participants >= event.capacity) {
                MaterialTheme.hack.danger
            } else {
                MaterialTheme.hack.success
            },
        )
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onOpenDetails),
        ) {
            Text(
                text = stringResource(R.string.feed_card_more),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun FeedError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.feed_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.feed_retry))
        }
    }
}

@Composable
private fun FeedEmpty(
    isFiltered: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.feed_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = if (isFiltered) {
                stringResource(R.string.feed_empty_filter_description)
            } else {
                stringResource(R.string.feed_empty_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
