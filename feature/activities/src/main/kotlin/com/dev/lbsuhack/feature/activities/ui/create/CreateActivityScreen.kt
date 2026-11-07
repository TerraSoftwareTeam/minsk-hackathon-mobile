package com.dev.lbsuhack.feature.activities.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.localization.R as LocalizationR
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.ui.component.HackSectionLabel
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityOrigin
import com.dev.lbsuhack.feature.activities.presentation.create.CreateActivityViewModel
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityScopeOrder
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStore
import com.dev.lbsuhack.feature.activities.store.create.CreationState
import com.dev.lbsuhack.feature.activities.store.create.MAX_VOTE_THRESHOLD
import com.dev.lbsuhack.feature.activities.ui.activities.mapper.labelRes
import com.dev.lbsuhack.feature.activities.ui.component.ActivityChanceMeter
import com.dev.lbsuhack.feature.activities.ui.component.ActivityTag
import com.dev.lbsuhack.feature.activities.ui.create.component.ActivityPhotoField
import com.dev.lbsuhack.feature.activities.ui.create.component.CategorySelector
import com.dev.lbsuhack.feature.activities.ui.create.component.CreateActivityCompletedState
import com.dev.lbsuhack.feature.activities.ui.create.component.CreateOriginBanner
import com.dev.lbsuhack.feature.activities.ui.create.component.FormSection
import com.dev.lbsuhack.feature.activities.ui.create.component.FormTextField
import com.dev.lbsuhack.feature.activities.ui.create.component.MaxParticipantsField
import com.dev.lbsuhack.feature.activities.ui.create.component.ParticipantCountRow
import com.dev.lbsuhack.feature.activities.ui.create.component.PhotoPendingState
import com.dev.lbsuhack.feature.activities.ui.create.component.SchoolDateField
import com.dev.lbsuhack.feature.activities.ui.create.component.SubmitActivityActions
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateActivityScreen(
    onBack: () -> Unit,
    onCreated: (activityId: String) -> Unit,
    viewModel: CreateActivityViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                is CreateActivityStore.Label.Created -> onCreated(label.activityId)
            }
        }
    }

    LaunchedEffect(state.errorId) {
        state.error?.let { errorText ->
            snackbarHostState.showSnackbar(errorText.resolve(context))
            viewModel.accept(CreateActivityStore.Intent.DismissError)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 108.dp)) },
        topBar = {
            HackTopBar(
                title = stringResource(
                    when (state.origin) {
                        ActivityOrigin.PUPIL -> R.string.create_activity_title
                        ActivityOrigin.TEACHER -> R.string.create_activity_title_teacher
                        ActivityOrigin.SCHOOL -> R.string.create_activity_title_school
                    },
                ),
                onBack = onBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            when (state.creationState) {
                CreationState.EDITING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CreateOriginBanner(origin = state.origin)

                        FormSection(title = stringResource(R.string.create_activity_main_section)) {
                            HackSectionLabel(text = stringResource(R.string.create_activity_category_label))
                            Spacer(modifier = Modifier.height(10.dp))
                            CategorySelector(
                                selected = state.category,
                                enabled = state.canEdit && !state.isSubmitting,
                                onSelect = { viewModel.accept(CreateActivityStore.Intent.ChangeCategory(it)) },
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            HackSectionLabel(text = stringResource(R.string.create_activity_photo_label))
                            Spacer(modifier = Modifier.height(10.dp))
                            ActivityPhotoField(
                                photoUri = state.photoUri,
                                title = state.title,
                                enabled = state.canEdit && !state.isSubmitting,
                                onChange = { viewModel.accept(CreateActivityStore.Intent.ChangePhoto(it)) },
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            FormTextField(
                                label = stringResource(R.string.create_activity_name_label),
                                hint = stringResource(R.string.create_activity_name_hint),
                                value = state.title,
                                onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeTitle(it)) },
                                singleLine = true,
                                enabled = state.canEdit && !state.isSubmitting,
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            FormTextField(
                                label = stringResource(R.string.create_activity_desc_label),
                                hint = stringResource(R.string.create_activity_desc_hint),
                                value = state.description,
                                onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeDescription(it)) },
                                modifier = Modifier.height(120.dp),
                                enabled = state.canEdit && !state.isSubmitting,
                            )
                        }

                        FormSection(title = stringResource(R.string.create_activity_participants_section)) {
                            val formEnabled = state.canEdit && !state.isSubmitting
                            state.startsAt?.let { startsAt ->
                                SchoolDateField(
                                    startsAt = startsAt,
                                    hasError = state.hasDateError,
                                    enabled = formEnabled,
                                    onChange = { viewModel.accept(CreateActivityStore.Intent.ChangeStartsAt(it)) },
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            ParticipantCountRow(
                                title = stringResource(R.string.create_activity_threshold_label),
                                value = state.voteThreshold,
                                decrementEnabled = state.voteThreshold > state.minVoteThreshold && formEnabled,
                                incrementEnabled = state.voteThreshold < MAX_VOTE_THRESHOLD && formEnabled,
                                onDecrement = {
                                    viewModel.accept(
                                        CreateActivityStore.Intent.ChangeVoteThreshold(state.voteThreshold - 1),
                                    )
                                },
                                onIncrement = {
                                    viewModel.accept(
                                        CreateActivityStore.Intent.ChangeVoteThreshold(state.voteThreshold + 1),
                                    )
                                },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(
                                    if (state.isStaff) {
                                        R.string.create_activity_threshold_hint_staff
                                    } else {
                                        R.string.create_activity_threshold_hint
                                    },
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MaxParticipantsField(
                                maxParticipants = state.maxParticipants,
                                minParticipants = state.voteThreshold,
                                maxAllowed = MAX_VOTE_THRESHOLD,
                                enabled = formEnabled,
                                onChange = { viewModel.accept(CreateActivityStore.Intent.ChangeMaxParticipants(it)) },
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            HackSectionLabel(text = stringResource(R.string.create_activity_scope_label))
                            Spacer(modifier = Modifier.height(10.dp))

                            if (state.fixedScope) {
                                ActivityTag(
                                    text = stringResource(R.string.create_activity_scope_school),
                                    icon = Icons.Outlined.Groups,
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    CreateActivityScopeOrder.forEach { scope ->
                                        FilterChip(
                                            selected = state.scope == scope,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                                selectedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = state.canEdit && !state.isSubmitting,
                                                selected = state.scope == scope,
                                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                                selectedBorderColor = MaterialTheme.colorScheme.onSurface,
                                                selectedBorderWidth = 1.dp,
                                            ),
                                            enabled = state.canEdit && !state.isSubmitting,
                                            onClick = { viewModel.accept(CreateActivityStore.Intent.ChangeScope(scope)) },
                                            label = {
                                                Text(stringResource(scope.labelRes()))
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        if (state.isChanceLoading || state.chance != null) {
                            FormSection(title = stringResource(LocalizationR.string.chance_preview_title)) {
                                ActivityChanceMeter(
                                    probability = state.chance.takeUnless { state.isChanceLoading },
                                    prominent = true,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(112.dp))
                    }
                    SubmitActivityActions(
                        submitText = stringResource(
                            if (state.isStaff) R.string.create_activity_publish_action else R.string.create_activity_submit_action,
                        ),
                        infoText = stringResource(
                            when (state.origin) {
                                ActivityOrigin.PUPIL -> R.string.create_activity_moderation_note
                                ActivityOrigin.TEACHER -> R.string.create_origin_teacher_desc
                                ActivityOrigin.SCHOOL -> R.string.create_origin_school_desc
                            },
                        ),
                        isSubmitting = state.isSubmitting,
                        onSubmit = { viewModel.accept(CreateActivityStore.Intent.Submit) },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                CreationState.PHOTO_PENDING -> {
                    PhotoPendingState(
                        isSubmitting = state.isSubmitting,
                        onRetryPhoto = { viewModel.accept(CreateActivityStore.Intent.Submit) },
                        onDropPhoto = {
                            viewModel.accept(CreateActivityStore.Intent.ChangePhoto(null))
                            viewModel.accept(CreateActivityStore.Intent.Submit)
                        },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                CreationState.CREATED -> {
                    CreateActivityCompletedState(
                        onOpenCreated = {
                            state.createdActivityId?.let(onCreated)
                        },
                        onBackToList = onBack,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}
