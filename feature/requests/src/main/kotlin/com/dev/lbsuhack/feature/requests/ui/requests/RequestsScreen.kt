package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.domain.isPending
import com.dev.lbsuhack.feature.requests.presentation.requests.RequestsViewModel
import com.dev.lbsuhack.feature.requests.store.requests.RequestDialog
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    role: UserRole,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit = {},
    viewModel: RequestsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel, role) {
        viewModel.labels.collect { label ->
            when (label) {
                is RequestsStore.Label.ShowMessage -> snackbarHostState.showSnackbar(label.text.resolve(context))
            }
        }
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.accept(RequestsStore.Intent.Load(role))
        onPauseOrDispose {}
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            when (state.audience) {
                                RequestAudience.TEACHER -> R.string.requests_title_teacher
                                RequestAudience.ADMIN -> R.string.requests_title_admin
                                null -> R.string.requests_title
                            },
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.accept(RequestsStore.Intent.Load(role)) }, enabled = !state.isLoading) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.requests_refresh))
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Default.Person, stringResource(R.string.requests_profile))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.requests_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { padding ->
        when {
            !state.isLoading && !state.canReview -> {
                AccessDenied(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            state.isLoading && state.requests.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                RequestsContent(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onSelectTab = { viewModel.accept(RequestsStore.Intent.SelectTab(it)) },
                    onApprove = { viewModel.accept(RequestsStore.Intent.ClickApprove(it)) },
                    onDecline = { viewModel.accept(RequestsStore.Intent.ClickDecline(it)) },
                )
            }
        }
    }

    val dialog = state.dialog
    if (dialog != null) {
        RequestsDialog(
            dialog = dialog,
            audience = state.audience,
            isSubmitting = state.isSubmitting,
            onDismiss = { viewModel.accept(RequestsStore.Intent.DismissDialog) },
            onChangeReason = { viewModel.accept(RequestsStore.Intent.ChangeDeclineReason(it)) },
            onChangeStartsAt = { viewModel.accept(RequestsStore.Intent.ChangeStartsAt(it)) },
            onConfirmApprove = { viewModel.accept(RequestsStore.Intent.ConfirmApprove) },
            onConfirmDecline = { viewModel.accept(RequestsStore.Intent.ConfirmDecline) },
        )
    }
}

@Composable
private fun RequestsContent(
    state: RequestsStore.State,
    modifier: Modifier = Modifier,
    onSelectTab: (RequestsTab) -> Unit,
    onApprove: (String) -> Unit,
    onDecline: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(
                when (state.audience) {
                    RequestAudience.TEACHER -> R.string.requests_subtitle_teacher
                    RequestAudience.ADMIN -> R.string.requests_subtitle_admin
                    null -> R.string.requests_subtitle_admin
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp),
        )

        RequestsTabs(
            selectedTab = state.selectedTab,
            pendingCount = state.pendingCount,
            onSelectTab = onSelectTab,
        )

        HorizontalDivider(color = MaterialTheme.hack.gridLine)

        if (state.visibleRequests.isEmpty()) {
            RequestsEmpty(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = state.visibleRequests,
                    key = { it.id },
                ) { request ->
                    RequestCard(
                        request = request,
                        onApprove = { onApprove(request.id) },
                        onDecline = { onDecline(request.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestsTabs(
    selectedTab: RequestsTab,
    pendingCount: Int,
    onSelectTab: (RequestsTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RequestsTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            val shape = MaterialTheme.shapes.medium
            Text(
                text = when (tab) {
                    RequestsTab.PENDING -> stringResource(R.string.requests_tab_pending, pendingCount)
                    RequestsTab.PROCESSED -> stringResource(R.string.requests_tab_processed)
                    RequestsTab.ALL -> stringResource(R.string.requests_tab_all)
                },
                modifier = Modifier
                    .clip(shape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.surfaceContainerHigh
                        else MaterialTheme.colorScheme.surfaceContainer,
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = shape,
                    )
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RequestCard(
    request: ReviewRequest,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
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
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RequestKindBadge(
                audience = request.audience,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(10.dp))
            RequestStatusBadge(status = request.status)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = request.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(10.dp))

        RequestInfoRow(
            icon = Icons.Outlined.Person,
            text = stringResource(
                R.string.requests_author_format,
                request.author.name,
                request.author.pupilClass,
            ),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = request.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    MaterialTheme.shapes.large,
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val proposedDate = request.proposedDate
            if (proposedDate != null) {
                RequestInfoRow(
                    icon = Icons.Outlined.Schedule,
                    text = stringResource(R.string.requests_date_format, proposedDate),
                )
            } else {
                RequestInfoRow(
                    icon = Icons.Outlined.Schedule,
                    text = stringResource(R.string.requests_date_not_set),
                )
            }
        }

        val declineReason = request.declineReason
        if (declineReason != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.requests_decline_reason_format, declineReason),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        MaterialTheme.shapes.large,
                    )
                    .padding(14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (request.isPending) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = stringResource(R.string.requests_action_decline),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                    )
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = stringResource(R.string.requests_action_approve),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestInfoRow(
    icon: ImageVector,
    text: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor,
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
        )
    }
}

@Composable
private fun RequestKindBadge(
    audience: RequestAudience,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    MaterialTheme.shapes.medium,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = when (audience) {
                    RequestAudience.TEACHER -> Icons.Outlined.Campaign
                    RequestAudience.ADMIN -> Icons.Outlined.Event
                },
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = stringResource(
                when (audience) {
                    RequestAudience.TEACHER -> R.string.requests_kind_publication
                    RequestAudience.ADMIN -> R.string.requests_kind_visit
                },
            ),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RequestStatusBadge(status: ReviewStatus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(statusColor(status), CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(
                when (status) {
                    ReviewStatus.PENDING_TEACHER -> R.string.requests_status_pending_teacher
                    ReviewStatus.PENDING_ADMIN -> R.string.requests_status_pending_admin
                    ReviewStatus.APPROVED -> R.string.requests_status_approved
                    ReviewStatus.DECLINED -> R.string.requests_status_declined
                },
            ),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun statusColor(status: ReviewStatus): Color = when (status) {
    ReviewStatus.PENDING_TEACHER -> MaterialTheme.colorScheme.primary
    ReviewStatus.PENDING_ADMIN -> MaterialTheme.colorScheme.onSurfaceVariant
    ReviewStatus.APPROVED -> MaterialTheme.colorScheme.primary
    ReviewStatus.DECLINED -> MaterialTheme.colorScheme.outline
}

@Composable
private fun RequestsEmpty(
    state: RequestsStore.State,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.requests_empty_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                when (state.selectedTab) {
                    RequestsTab.PENDING -> when (state.audience) {
                        RequestAudience.TEACHER -> R.string.requests_empty_pending_teacher
                        RequestAudience.ADMIN -> R.string.requests_empty_pending_admin
                        null -> R.string.requests_empty_all
                    }
                    RequestsTab.PROCESSED -> R.string.requests_empty_processed
                    RequestsTab.ALL -> R.string.requests_empty_all
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AccessDenied(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.requests_access_denied_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.requests_access_denied_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RequestsDialog(
    dialog: RequestDialog,
    audience: RequestAudience?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onChangeReason: (String) -> Unit,
    onChangeStartsAt: (String) -> Unit,
    onConfirmApprove: () -> Unit,
    onConfirmDecline: () -> Unit,
) {
    when (dialog) {
        is RequestDialog.Approve -> AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) onDismiss()
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = {
                Text(
                    text = stringResource(R.string.requests_dialog_approve_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = dialog.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            if (audience == RequestAudience.ADMIN) {
                                R.string.requests_dialog_approve_admin_text
                            } else {
                                R.string.requests_dialog_approve_teacher_text
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    val startsAt = dialog.startsAt
                    if (startsAt != null) {
                        Text(
                            text = stringResource(R.string.requests_dialog_approve_date_label),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                        RequestDateTimeField(
                            startsAt = startsAt,
                            enabled = !isSubmitting,
                            onChange = onChangeStartsAt,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmApprove,
                    enabled = !isSubmitting,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.requests_action_approve),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                ) {
                    Text(stringResource(R.string.requests_action_cancel))
                }
            },
        )

        is RequestDialog.Decline -> AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) onDismiss()
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = {
                Text(
                    text = stringResource(R.string.requests_dialog_decline_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = dialog.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.requests_dialog_decline_text),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    DialogTextField(
                        value = dialog.reason,
                        onValueChange = onChangeReason,
                        label = stringResource(R.string.requests_dialog_decline_reason_label),
                        hint = stringResource(R.string.requests_dialog_decline_reason_hint),
                        minLines = 2,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDecline,
                    enabled = !isSubmitting,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onError,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.requests_action_decline),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                ) {
                    Text(stringResource(R.string.requests_action_cancel))
                }
            },
        )
    }
}

@Composable
private fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String? = null,
    minLines: Int = 1,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = if (hint == null) {
                null
            } else {
                {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            },
            minLines = minLines,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }
}
