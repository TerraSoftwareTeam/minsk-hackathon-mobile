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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.dev.lbsuhack.core.designsystem.theme.hack
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        bottomBar = {
            SubmitActivityBar(
                isSubmitting = state.isSubmitting,
                onSubmit = { viewModel.accept(CreateActivityStore.Intent.Submit) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
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

                FormTextField(
                    label = stringResource(R.string.create_activity_title_label),
                    hint = stringResource(R.string.create_activity_title_hint),
                    value = state.title,
                    onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeTitle(it)) },
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(18.dp))

                FormTextField(
                    label = stringResource(R.string.create_activity_desc_label),
                    hint = stringResource(R.string.create_activity_desc_hint),
                    value = state.description,
                    onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeDescription(it)) },
                    modifier = Modifier.height(132.dp),
                )
            }

            FormSection(title = stringResource(R.string.create_activity_participants_section)) {
                FieldLabel(text = stringResource(R.string.create_activity_limits_label))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NumberStepperCard(
                        title = stringResource(R.string.create_activity_min_title),
                        value = state.minParticipants,
                        decrementEnabled = state.minParticipants > 2,
                        incrementEnabled = state.minParticipants < 50,
                        onDecrement = {
                            viewModel.accept(
                                CreateActivityStore.Intent.ChangeMinParticipants(state.minParticipants - 1),
                            )
                        },
                        onIncrement = {
                            viewModel.accept(
                                CreateActivityStore.Intent.ChangeMinParticipants(state.minParticipants + 1),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )

                    NumberStepperCard(
                        title = stringResource(R.string.create_activity_max_title),
                        value = state.maxParticipants,
                        decrementEnabled = state.maxParticipants > state.minParticipants,
                        incrementEnabled = state.maxParticipants < 100,
                        onDecrement = {
                            viewModel.accept(
                                CreateActivityStore.Intent.ChangeMaxParticipants(state.maxParticipants - 1),
                            )
                        },
                        onIncrement = {
                            viewModel.accept(
                                CreateActivityStore.Intent.ChangeMaxParticipants(state.maxParticipants + 1),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                FormTextField(
                    label = stringResource(R.string.create_activity_target_class_label),
                    hint = stringResource(R.string.create_activity_target_class_hint),
                    value = state.targetClass,
                    onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeTargetClass(it)) },
                    singleLine = true,
                )
            }

            FormSection(title = stringResource(R.string.create_activity_organization_section)) {
                FormTextField(
                    label = stringResource(R.string.create_activity_preferences_label),
                    hint = stringResource(R.string.create_activity_preferences_hint),
                    value = state.preferenceNotes,
                    onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangePreferences(it)) },
                    modifier = Modifier.height(104.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
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

@Composable
private fun NumberStepperCard(
    title: String,
    value: Int,
    decrementEnabled: Boolean,
    incrementEnabled: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large,
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            StepperButton(
                icon = Icons.Default.Remove,
                enabled = decrementEnabled,
                onClick = onDecrement,
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            StepperButton(
                icon = Icons.Default.Add,
                enabled = incrementEnabled,
                onClick = onIncrement,
            )
        }
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        )
    }
}

@Composable
private fun SubmitActivityBar(
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = MaterialTheme.hack.gridLine)
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.create_activity_moderation_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    enabled = !isSubmitting,
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.create_activity_submit_action),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}
