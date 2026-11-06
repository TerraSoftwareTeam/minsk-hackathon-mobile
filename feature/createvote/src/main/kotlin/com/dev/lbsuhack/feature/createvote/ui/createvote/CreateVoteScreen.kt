package com.dev.lbsuhack.feature.createvote.ui.createvote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.HackMonoTextStyle
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.ui.component.HackEmptyState
import com.dev.lbsuhack.core.ui.component.HackFieldLabel
import com.dev.lbsuhack.core.ui.component.HackLoading
import com.dev.lbsuhack.core.ui.component.HackPrimaryButton
import com.dev.lbsuhack.core.ui.component.HackSectionLabel
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.feature.createvote.R
import com.dev.lbsuhack.feature.createvote.domain.VoteEvent
import com.dev.lbsuhack.feature.createvote.domain.VoteParticipant
import com.dev.lbsuhack.feature.createvote.presentation.createvote.CreateVoteViewModel
import com.dev.lbsuhack.feature.createvote.store.createvote.CreateVoteStore
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateVoteScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateVoteViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.accept(CreateVoteStore.Intent.Load)
        viewModel.labels.collect { label ->
            when (label) {
                CreateVoteStore.Label.Created -> onCreated()
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { errorText ->
            snackbarHostState.showSnackbar(errorText.resolve(context))
            viewModel.accept(CreateVoteStore.Intent.DismissError)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HackTopBar(
                title = stringResource(R.string.create_vote_title),
                onBack = onBack,
                backContentDescription = stringResource(R.string.create_vote_back),
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.events.isEmpty() -> {
                HackLoading(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            state.events.isEmpty() -> {
                HackEmptyState(
                    title = stringResource(R.string.create_vote_empty_events),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    icon = Icons.Default.Event,
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = stringResource(R.string.create_vote_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    HackSectionLabel(stringResource(R.string.create_vote_event_section))
                    EventSelector(
                        events = state.events,
                        selectedEventId = state.selectedEventId,
                        onSelect = { viewModel.accept(CreateVoteStore.Intent.SelectEvent(it)) },
                    )

                    HackSectionLabel(stringResource(R.string.create_vote_info_section))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        HackFieldLabel(stringResource(R.string.create_vote_title_label))
                        VoteInput(
                            value = state.title,
                            onValueChange = { viewModel.accept(CreateVoteStore.Intent.ChangeTitle(it)) },
                            hint = stringResource(R.string.create_vote_title_hint),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        HackFieldLabel(stringResource(R.string.create_vote_description_label))
                        VoteInput(
                            value = state.description,
                            onValueChange = { viewModel.accept(CreateVoteStore.Intent.ChangeDescription(it)) },
                            hint = stringResource(R.string.create_vote_description_hint),
                            singleLine = false,
                            modifier = Modifier.height(110.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        HackFieldLabel(stringResource(R.string.create_vote_deadline_label))
                        VoteInput(
                            value = state.deadline,
                            onValueChange = { viewModel.accept(CreateVoteStore.Intent.ChangeDeadline(it)) },
                            hint = stringResource(R.string.create_vote_deadline_hint),
                        )
                    }

                    HackSectionLabel(stringResource(R.string.create_vote_participants_section))
                    ParticipantsHeader(
                        selectedCount = state.selectedParticipantIds.size,
                        totalCount = state.availableParticipants.size,
                        allSelected = state.allParticipantsSelected,
                        onToggleAll = { viewModel.accept(CreateVoteStore.Intent.ToggleAllParticipants) },
                    )
                    if (state.availableParticipants.isEmpty()) {
                        Text(
                            text = stringResource(R.string.create_vote_participants_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            state.availableParticipants.forEach { participant ->
                                ParticipantRow(
                                    participant = participant,
                                    checked = state.selectedParticipantIds.contains(participant.id),
                                    onToggle = {
                                        viewModel.accept(CreateVoteStore.Intent.ToggleParticipant(participant.id))
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    HackPrimaryButton(
                        text = stringResource(R.string.create_vote_submit),
                        onClick = { viewModel.accept(CreateVoteStore.Intent.Submit) },
                        loading = state.isSubmitting,
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun VoteInput(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        singleLine = singleLine,
    )
}

@Composable
private fun EventSelector(
    events: List<VoteEvent>,
    selectedEventId: String?,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        events.forEach { event ->
            val selected = event.id == selectedEventId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .clickable { onSelect(event.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected,
                    onClick = { onSelect(event.id) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.date,
                        style = HackMonoTextStyle.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.create_vote_event_participants,
                        event.participants.size,
                    ),
                    style = HackMonoTextStyle.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ParticipantsHeader(
    selectedCount: Int,
    totalCount: Int,
    allSelected: Boolean,
    onToggleAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                R.string.create_vote_participants_count,
                selectedCount,
                totalCount,
            ),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick = onToggleAll,
            enabled = totalCount > 0,
        ) {
            Text(
                text = stringResource(
                    if (allSelected) R.string.create_vote_clear_all else R.string.create_vote_select_all,
                ),
            )
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: VoteParticipant,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = participant.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = participant.pupilClass,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

