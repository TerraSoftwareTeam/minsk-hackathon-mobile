package com.dev.lbsuhack.feature.feed.ui.feed

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.subscribedEvents.isNotEmpty()) {
                            MyEventsSection(
                                events = state.subscribedEvents,
                                onOpenDetails = { eventId ->
                                    viewModel.accept(FeedStore.Intent.ClickEvent(eventId))
                                },
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            FeedEmpty(isFiltered = state.events.isNotEmpty())
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
                    ) {
                        if (state.subscribedEvents.isNotEmpty()) {
                            item(key = "my_events") {
                                MyEventsSection(
                                    events = state.subscribedEvents,
                                    onOpenDetails = { eventId ->
                                        viewModel.accept(FeedStore.Intent.ClickEvent(eventId))
                                    },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.hack.gridLine,
                                )
                            }
                        }
                        itemsIndexed(
                            state.visibleEvents,
                            key = { _, event -> event.id },
                        ) { index, event ->
                            EventListItem(
                                event = event,
                                onClick = {
                                    viewModel.accept(FeedStore.Intent.ClickEvent(event.id))
                                },
                            )
                            if (index < state.visibleEvents.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.hack.gridLine,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedTopBar(
    selectedCategory: EventCategory?,
    onSelectCategory: (EventCategory?) -> Unit,
    onOpenProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 10.dp),
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
                    text = stringResource(R.string.feed_subtitle),
                    style = MaterialTheme.typography.bodySmall,
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
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.hack.gridLine)
    }
}

@Composable
private fun ProfileAvatar(onOpenProfile: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onOpenProfile),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = CurrentUserInitials,
            style = MaterialTheme.typography.titleSmall,
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
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
private fun MyEventsSection(
    events: List<FeedEvent>,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feed_my_events),
            style = HackMonoTextStyle.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(events, key = FeedEvent::id) { event ->
                MyEventCard(
                    event = event,
                    onClick = { onOpenDetails(event.id) },
                )
            }
        }
    }
}

@Composable
private fun MyEventCard(
    event: FeedEvent,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .size(width = 232.dp, height = 116.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.hack.success,
            )
            Text(
                text = stringResource(R.string.feed_card_subscribed),
                style = HackMonoTextStyle.copy(fontSize = 11.sp),
                color = MaterialTheme.hack.success,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${event.day} ${event.month}",
                style = HackMonoTextStyle.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = event.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = event.time,
            style = HackMonoTextStyle.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EventListItem(
    event: FeedEvent,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(
            text = "${event.day} ${event.month}",
            style = HackMonoTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = event.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${event.time} · ${event.place}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = event.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        EventStatus(event = event)
    }
}

@Composable
private fun EventStatus(event: FeedEvent) {
    val seatsLeft = event.capacity - event.participants

    if (seatsLeft <= 0) {
        Text(
            text = stringResource(R.string.feed_no_seats),
            style = HackMonoTextStyle.copy(fontSize = 12.sp),
            color = MaterialTheme.hack.danger,
        )
    } else {
        Text(
            text = pluralStringResource(
                R.plurals.feed_seats_available,
                seatsLeft,
                seatsLeft,
            ),
            style = HackMonoTextStyle.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
