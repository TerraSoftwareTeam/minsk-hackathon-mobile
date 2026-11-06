package com.dev.lbsuhack.feature.activevote.ui.activevote

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.ui.component.HackEmptyState
import com.dev.lbsuhack.core.ui.component.HackEventCard
import com.dev.lbsuhack.core.ui.component.HackLoading
import com.dev.lbsuhack.core.ui.component.HackSectionLabel
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.feature.activevote.R
import com.dev.lbsuhack.feature.activevote.domain.ActiveVote
import com.dev.lbsuhack.feature.activevote.domain.ActiveVoteEvent
import com.dev.lbsuhack.feature.activevote.domain.ActiveVoteUser
import com.dev.lbsuhack.feature.activevote.domain.VoteOption
import com.dev.lbsuhack.feature.activevote.presentation.activevote.ActiveVoteViewModel
import com.dev.lbsuhack.feature.activevote.store.activevote.ActiveVoteStore
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActiveVoteScreen(
    voteId: String,
    onBack: () -> Unit,
    viewModel: ActiveVoteViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val successMessage = stringResource(R.string.active_vote_submitted_success)

    LaunchedEffect(voteId) {
        viewModel.accept(ActiveVoteStore.Intent.Load(voteId))
    }

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                is ActiveVoteStore.Label.VoteAccepted -> {
                    snackbarHostState.showSnackbar(successMessage)
                }
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { errorText ->
            snackbarHostState.showSnackbar(errorText.resolve(context))
            viewModel.accept(ActiveVoteStore.Intent.DismissError)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HackTopBar(
                title = state.vote?.eventTitle ?: stringResource(R.string.active_vote_title),
                onBack = onBack,
                backContentDescription = stringResource(R.string.active_vote_back),
            )
        },
        floatingActionButton = {
            if (state.vote != null && !state.isLoading) {
                ActiveVoteFloatingButton(
                    hasVoted = state.hasVoted,
                    canSubmit = state.selectedOptionId != null && !state.isSubmitting,
                    onSubmit = { viewModel.accept(ActiveVoteStore.Intent.Submit) },
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> {
                HackLoading(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            state.vote == null -> {
                HackEmptyState(
                    title = stringResource(R.string.active_vote_stub_title),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    icon = Icons.Default.HowToVote,
                    description = stringResource(R.string.active_vote_stub_description),
                )
            }

            else -> {
                val vote = state.vote!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    if (vote.events.isNotEmpty()) {
                        item(key = "events_header") {
                            HackSectionLabel(text = stringResource(R.string.active_vote_events_section))
                        }
                        items(vote.events, key = ActiveVoteEvent::id) { event ->
                            val seatsLeft = (event.capacity - event.participants).coerceAtLeast(0)
                            val isFull = seatsLeft == 0
                            val capacityBadgeText = if (isFull) {
                                stringResource(R.string.active_vote_no_seats)
                            } else {
                                stringResource(R.string.active_vote_seats_left, seatsLeft)
                            }

                            HackEventCard(
                                day = event.day,
                                month = event.month,
                                category = event.category,
                                title = event.title,
                                summary = event.summary,
                                time = event.time,
                                place = event.place,
                                participantsLabel = stringResource(R.string.active_vote_participants_label),
                                participantsText = "${event.participants} / ${event.capacity}",
                                participantsProgress = if (event.capacity > 0) {
                                    event.participants.toFloat() / event.capacity
                                } else {
                                    0f
                                },
                                capacityBadgeText = capacityBadgeText,
                                isFull = isFull,
                                actionText = stringResource(R.string.active_vote_event_details),
                                onActionClick = {},
                            )
                        }
                    }

                    item(key = "voting_section") {
                        VotingBlock(
                            question = vote.question,
                            deadline = vote.deadline,
                            options = vote.options,
                            selectedOptionId = state.selectedOptionId,
                            hasVoted = state.hasVoted,
                            onSelectOption = { optionId ->
                                viewModel.accept(ActiveVoteStore.Intent.SelectOption(optionId))
                            },
                        )
                    }

                    if (vote.users.isNotEmpty()) {
                        item(key = "users_section") {
                            UsersBlock(
                                users = vote.users,
                            )
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VotingBlock(
    question: String,
    deadline: String,
    options: List<VoteOption>,
    selectedOptionId: String?,
    hasVoted: Boolean,
    onSelectOption: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HackSectionLabel(
                text = stringResource(R.string.active_vote_question_section),
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = deadline,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { option ->
                        val isSelected = selectedOptionId == option.id
                        val borderColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                        val containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        }

                        Surface(
                            onClick = { onSelectOption(option.id) },
                            enabled = !hasVoted,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = containerColor,
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    enabled = !hasVoted,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                if (hasVoted && isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.hack.successContainer,
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.hack.success,
                                            )
                                            Text(
                                                text = stringResource(R.string.active_vote_your_choice),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.hack.success,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersBlock(
    users: List<ActiveVoteUser>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HackSectionLabel(
                text = stringResource(R.string.active_vote_users_section),
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "${users.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column {
                users.forEachIndexed { index, user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val initials = user.name
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .mapNotNull { it.firstOrNull() }
                            .take(2)
                            .joinToString("")

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (user.hasVoted) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.hack.successContainer,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.hack.success,
                                    )
                                    Text(
                                        text = stringResource(R.string.active_vote_user_status_voted),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.hack.success,
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Text(
                                    text = stringResource(R.string.active_vote_user_status_waiting),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    if (index < users.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveVoteFloatingButton(
    hasVoted: Boolean,
    canSubmit: Boolean,
    onSubmit: () -> Unit,
) {
    if (hasVoted) {
        ExtendedFloatingActionButton(
            onClick = {},
            containerColor = MaterialTheme.hack.successContainer,
            contentColor = MaterialTheme.hack.success,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.active_vote_submitted_button),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
        )
    } else {
        ExtendedFloatingActionButton(
            onClick = onSubmit,
            containerColor = if (canSubmit) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = if (canSubmit) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.HowToVote,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.active_vote_submit_button),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
        )
    }
}

