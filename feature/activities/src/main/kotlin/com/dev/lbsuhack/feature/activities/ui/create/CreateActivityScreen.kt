package com.dev.lbsuhack.feature.activities.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.presentation.create.CreateActivityViewModel
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStore
import com.dev.lbsuhack.feature.activities.ui.activities.icon
import com.dev.lbsuhack.feature.activities.ui.activities.labelRes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(state.error) {
        state.error?.let { errorText ->
            snackbarHostState.showSnackbar(errorText.resolve(context))
            viewModel.accept(CreateActivityStore.Intent.DismissError)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 108.dp)) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_activity_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FormSection(title = stringResource(R.string.create_activity_main_section)) {
                    FieldLabel(text = stringResource(R.string.create_activity_category_label))
                    Spacer(modifier = Modifier.height(10.dp))
                    CategorySelector(
                        selected = state.category,
                        onSelect = { viewModel.accept(CreateActivityStore.Intent.ChangeCategory(it)) },
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FieldLabel(text = stringResource(R.string.create_activity_photo_label))
                    Spacer(modifier = Modifier.height(10.dp))
                    ActivityPhotoField(
                        photoUri = state.photoUri,
                        title = state.title,
                        enabled = !state.isSubmitting,
                        onChange = { viewModel.accept(CreateActivityStore.Intent.ChangePhoto(it)) },
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FormTextField(
                        label = stringResource(R.string.create_activity_name_label),
                        hint = stringResource(R.string.create_activity_name_hint),
                        value = state.title,
                        onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeTitle(it)) },
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FormTextField(
                        label = stringResource(R.string.create_activity_desc_label),
                        hint = stringResource(R.string.create_activity_desc_hint),
                        value = state.description,
                        onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeDescription(it)) },
                        modifier = Modifier.height(120.dp),
                    )
                }

                if (state.createdActivityId == null) {
                    FormSection(title = stringResource(R.string.create_activity_participants_section)) {
                        ParticipantCountRow(
                            title = stringResource(R.string.create_activity_threshold_label),
                            value = state.voteThreshold,
                            decrementEnabled = state.voteThreshold > 1 && !state.isSubmitting,
                            incrementEnabled = state.voteThreshold < 10000 && !state.isSubmitting,
                            onDecrement = { viewModel.accept(CreateActivityStore.Intent.ChangeVoteThreshold(state.voteThreshold - 1)) },
                            onIncrement = { viewModel.accept(CreateActivityStore.Intent.ChangeVoteThreshold(state.voteThreshold + 1)) },
                        )
                        FieldLabel(text = stringResource(R.string.create_activity_scope_label))
                        ActivityScope.entries.forEach { scope ->
                            FilterChip(
                                selected = state.scope == scope,
                                enabled = !state.isSubmitting,
                                onClick = { viewModel.accept(CreateActivityStore.Intent.ChangeScope(scope)) },
                                label = { Text(stringResource(when (scope) {
                                    ActivityScope.CLASS -> R.string.create_activity_scope_class
                                    ActivityScope.GRADE -> R.string.create_activity_scope_grade
                                    ActivityScope.SCHOOL -> R.string.create_activity_scope_school
                                })) },
                            )
                        }
                    }
                } else {
                    Text(stringResource(R.string.create_activity_photo_pending))
                }

                Spacer(modifier = Modifier.height(112.dp))
            }
            SubmitActivityActions(
                isSubmitting = state.isSubmitting,
                onSubmit = { viewModel.accept(CreateActivityStore.Intent.Submit) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
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
            .padding(18.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(18.dp))
        content()
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun FormTextField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
) {
    Column {
        FieldLabel(text = label)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = hint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            },
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            singleLine = singleLine,
        )
    }
}

@Composable
private fun CategorySelector(
    selected: ActivityCategory,
    onSelect: (ActivityCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ActivityCategory.entries.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowCategories.forEach { category ->
                    val isSelected = selected == category
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.large)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surfaceContainerLow,
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.large,
                            )
                            .clickable { onSelect(category) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = category.icon(),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = stringResource(category.labelRes()),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
