package com.dev.lbsuhack.feature.feed.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.core.localization.text.asString
import com.dev.lbsuhack.feature.feed.R
import com.dev.lbsuhack.feature.feed.domain.FeedEvent
import com.dev.lbsuhack.feature.feed.presentation.details.FeedDetailsViewModel
import com.dev.lbsuhack.feature.feed.store.details.FeedDetailsStore
import com.dev.lbsuhack.feature.feed.ui.feed.CapacityBadge
import com.dev.lbsuhack.feature.feed.ui.feed.EventDateBadge
import com.dev.lbsuhack.feature.feed.ui.feed.EventTag
import com.dev.lbsuhack.feature.feed.ui.feed.SubscribedBadge
import com.dev.lbsuhack.feature.feed.ui.feed.accent
import com.dev.lbsuhack.feature.feed.ui.feed.labelRes
import org.koin.androidx.compose.koinViewModel

@Composable
fun FeedDetailsScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: FeedDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) {
        viewModel.accept(FeedDetailsStore.Intent.Load(eventId))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { DetailsTopBar(onBack = onBack) },
        bottomBar = {
            state.event?.let { event ->
                SubscribeBar(
                    event = event,
                    isSubscribing = state.isSubscribing,
                    onToggle = { viewModel.accept(FeedDetailsStore.Intent.ToggleSubscription) },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading && state.event == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null && state.event == null -> {
                    DetailsError(
                        message = state.error!!.asString(),
                        onRetry = { viewModel.accept(FeedDetailsStore.Intent.Retry) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    state.event?.let { event -> EventContent(event = event) }
                }
            }
        }
    }
}

@Composable
private fun DetailsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.feed_details_back),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.feed_details_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun DetailsError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.feed_retry))
        }
    }
}

@Composable
private fun EventContent(event: FeedEvent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        EventHero(event = event)
        EventInfoCard(event = event)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.feed_details_about),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (event.tags.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                event.tags.forEach { tag -> EventTag(text = tag) }
            }
        }

        ParticipantsCard(event = event)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun EventHero(event: FeedEvent) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                EventDateBadge(event = event)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(event.category.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        color = event.category.accent().accent,
                    )
                    if (event.isSubscribed) {
                        SubscribedBadge()
                    }
                }
            }
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = event.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EventInfoCard(event: FeedEvent) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            InfoRow(
                icon = Icons.Outlined.Schedule,
                label = stringResource(R.string.feed_details_time),
                value = "${event.day} ${event.month}, ${event.time}",
            )
            InfoRow(
                icon = Icons.Outlined.LocationOn,
                label = stringResource(R.string.feed_details_place),
                value = event.place,
            )
            InfoRow(
                icon = Icons.Outlined.Person,
                label = stringResource(R.string.feed_details_organizer),
                value = event.organizer,
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.shapes.small,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ParticipantsCard(event: FeedEvent) {
    val isFull = event.participants >= event.capacity
    val progress = if (event.capacity > 0) {
        (event.participants.toFloat() / event.capacity).coerceIn(0f, 1f)
    } else {
        0f
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.feed_details_participants),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${event.participants} / ${event.capacity}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isFull) {
                        MaterialTheme.hack.danger
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                strokeCap = StrokeCap.Round,
                color = if (isFull) {
                    MaterialTheme.hack.danger
                } else {
                    event.category.accent().accent
                },
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                CapacityBadge(event = event)
            }
        }
    }
}

@Composable
private fun SubscribeBar(
    event: FeedEvent,
    isSubscribing: Boolean,
    onToggle: () -> Unit,
) {
    val isFull = event.participants >= event.capacity && !event.isSubscribed
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Button(
                onClick = onToggle,
                enabled = !isSubscribing && !isFull,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .height(54.dp),
                shape = MaterialTheme.shapes.medium,
                colors = if (event.isSubscribed) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.hack.successContainer,
                        contentColor = MaterialTheme.hack.success,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) {
                if (isSubscribing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.feed_details_subscribing),
                        style = MaterialTheme.typography.titleSmall,
                    )
                } else if (event.isSubscribed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.feed_details_subscribed),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text(
                        text = stringResource(
                            if (isFull) R.string.feed_no_seats else R.string.feed_details_subscribe,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}
