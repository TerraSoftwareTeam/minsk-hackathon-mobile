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
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.hack.gridLine),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Button(
                        onClick = { viewModel.accept(CreateActivityStore.Intent.Submit) },
                        enabled = !state.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp,
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
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.large,
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.HourglassEmpty,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.create_activity_moderation_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

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

                Spacer(modifier = Modifier.height(16.dp))

                FormTextField(
                    label = stringResource(R.string.create_activity_desc_label),
                    hint = stringResource(R.string.create_activity_desc_hint),
                    value = state.description,
                    onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeDescription(it)) },
                    modifier = Modifier.height(110.dp),
                )
            }

            FormSection(title = stringResource(R.string.create_activity_participants_section)) {
                FieldLabel(text = stringResource(R.string.create_activity_scope_label))
                Spacer(modifier = Modifier.height(10.dp))
                ScopeSelector(
                    selected = state.scope,
                    onSelect = { viewModel.accept(CreateActivityStore.Intent.ChangeScope(it)) },
                )

                Spacer(modifier = Modifier.height(20.dp))

                NumberStepperCard(
                    title = stringResource(R.string.create_activity_threshold_label),
                    value = state.voteThreshold,
                    decrementEnabled = state.voteThreshold > 1,
                    incrementEnabled = state.voteThreshold < 1000,
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
            }

            FormSection(title = stringResource(R.string.create_activity_organization_section)) {
                FormTextField(
                    label = stringResource(R.string.create_activity_date_label),
                    hint = "2026-11-21T10:00:00.000Z",
                    value = state.startsAt,
                    onValueChange = { viewModel.accept(CreateActivityStore.Intent.ChangeStartsAt(it)) },
                    singleLine = true,
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
private fun ScopeSelector(
    selected: ActivityScope,
    onSelect: (ActivityScope) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ActivityScope.entries.forEach { scope ->
            val isSelected = selected == scope
            val label = when (scope) {
                ActivityScope.CLASS -> stringResource(R.string.create_activity_scope_class)
                ActivityScope.GRADE -> stringResource(R.string.create_activity_scope_grade)
                ActivityScope.SCHOOL -> stringResource(R.string.create_activity_scope_school)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .clickable { onSelect(scope) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large,
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
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
            .size(38.dp)
            .clip(CircleShape)
            .background(
                if (enabled) MaterialTheme.colorScheme.surfaceContainerHigh
                else MaterialTheme.colorScheme.surfaceContainerLow,
            )
            .border(
                width = 1.dp,
                color = if (enabled) MaterialTheme.colorScheme.outlineVariant
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = CircleShape,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp),
        )
    }
}
