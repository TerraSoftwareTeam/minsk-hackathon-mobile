package com.dev.lbsuhack.feature.feed.ui.feed

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.HackAccent
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

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (state.subscribedEvents.isNotEmpty()) {
                            item(key = "my_events") {
                                MyEventsSection(
                                    events = state.subscribedEvents,
                                    onOpenDetails = { eventId ->
                                        viewModel.accept(FeedStore.Intent.ClickEvent(eventId))
                                    },
                                )
                            }
                        }

                        if (state.visibleEvents.isEmpty()) {
                            item(key = "empty_feed") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    FeedEmpty(isFiltered = state.events.isNotEmpty())
                                }
                            }
                        } else {
                            items(state.visibleEvents, key = FeedEvent::id) { event ->
                                EventCard(
                                    event = event,
                                    onClick = {
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
            .padding(top = 20.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.feed_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.feed_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            ProfileAvatar(onOpenProfile = onOpenProfile)
        }
        Spacer(modifier = Modifier.height(20.dp))
        FeedFilters(
            selectedCategory = selectedCategory,
            onSelectCategory = onSelectCategory,
        )
    }
}

@Composable
private fun ProfileAvatar(onOpenProfile: () -> Unit) {
    val openProfileLabel = stringResource(R.string.feed_open_profile)
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                shape = CircleShape,
            )
            .clickable(
                onClickLabel = openProfileLabel,
                onClick = onOpenProfile,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = CurrentUserInitials,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun FeedFilters(
    selectedCategory: EventCategory?,
    onSelectCategory: (EventCategory?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
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
        shape = CircleShape,
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
internal fun EventCategory.accent(): HackAccent {
    val extras = MaterialTheme.hack
    return when (this) {
        EventCategory.HACKATHON -> extras.violet
        EventCategory.MEETUP -> extras.amber
        EventCategory.WORKSHOP -> extras.green
        EventCategory.LECTURE -> extras.pink
    }
}

@Composable
private fun MyEventsSection(
    events: List<FeedEvent>,
    onOpenDetails: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feed_my_events),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
    val categoryAccent = event.category.accent()
    Surface(
        onClick = onClick,
        modifier = Modifier.width(264.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${event.day} ${event.month}",
                    style = MaterialTheme.typography.labelLarge,
                    color = categoryAccent.accent,
                )
                Spacer(modifier = Modifier.weight(1f))
                SubscribedBadge()
            }
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${event.time} · ${event.place}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EventCard(
    event: FeedEvent,
    onClick: () -> Unit,
) {
    val categoryAccent = event.category.accent()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                EventDateBadge(event = event)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(event.category.labelRes()),
                        style = MaterialTheme.typography.labelMedium,
                        color = categoryAccent.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Text(
                text = event.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EventMetaRow(
                    icon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                    text = event.time,
                )
                EventMetaRow(
                    icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    text = event.place,
                )
            }

            ParticipantsProgress(event = event)

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = stringResource(R.string.feed_open_event),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun EventMetaRow(
    icon: @Composable () -> Unit,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
            icon()
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun EventDateBadge(
    event: FeedEvent,
    modifier: Modifier = Modifier,
) {
    val categoryAccent = event.category.accent()
    Surface(
        modifier = modifier.size(width = 60.dp, height = 68.dp),
        shape = MaterialTheme.shapes.medium,
        color = categoryAccent.container,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = event.day,
                style = MaterialTheme.typography.titleLarge,
                color = categoryAccent.accent,
            )
            Text(
                text = event.month.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = categoryAccent.accent,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ParticipantsProgress(event: FeedEvent) {
    val progress = if (event.capacity > 0) {
        (event.participants.toFloat() / event.capacity).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.feed_details_participants),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${event.participants} / ${event.capacity}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            CapacityBadge(event = event)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = event.category.accent().accent,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
internal fun CapacityBadge(event: FeedEvent) {
    val seatsLeft = (event.capacity - event.participants).coerceAtLeast(0)
    val isFull = seatsLeft == 0
    val background = if (isFull) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val foreground = if (isFull) {
        MaterialTheme.hack.danger
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val label = if (isFull) {
        stringResource(R.string.feed_no_seats)
    } else {
        pluralStringResource(R.plurals.feed_seats_available, seatsLeft, seatsLeft)
    }
    StatusBadge(
        text = label,
        background = background,
        foreground = foreground,
    )
}

@Composable
internal fun SubscribedBadge() {
    StatusBadge(
        text = stringResource(R.string.feed_card_subscribed),
        background = MaterialTheme.hack.successContainer,
        foreground = MaterialTheme.hack.success,
        leadingIcon = true,
    )
}

@Composable
private fun StatusBadge(
    text: String,
    background: Color,
    foreground: Color,
    leadingIcon: Boolean = false,
) {
    Surface(
        shape = CircleShape,
        color = background,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (leadingIcon) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = foreground,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = foreground,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun EventTag(text: String) {
    Text(
        text = "#$text",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 6.dp),
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
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
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
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = if (isFiltered) {
                stringResource(R.string.feed_empty_filter_description)
            } else {
                stringResource(R.string.feed_empty_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
