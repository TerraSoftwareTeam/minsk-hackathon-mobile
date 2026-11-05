package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.HackMonoTextStyle
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
    viewModel: RequestsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel, role) {
        viewModel.accept(RequestsStore.Intent.Load(role))
        viewModel.labels.collect { label ->
            when (label) {
                is RequestsStore.Label.ShowMessage -> snackbarHostState.showSnackbar(label.text.resolve(context))
            }
        }
    }

    Scaffold(
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
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.requests_back),
                        )
                    }
                },
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
            isSubmitting = state.isSubmitting,
            onDismiss = { viewModel.accept(RequestsStore.Intent.DismissDialog) },
            onChangeDate = { viewModel.accept(RequestsStore.Intent.ChangeConfirmedDate(it)) },
            onChangeLocation = { viewModel.accept(RequestsStore.Intent.ChangeConfirmedLocation(it)) },
            onChangeReason = { viewModel.accept(RequestsStore.Intent.ChangeDeclineReason(it)) },
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
        )

        RequestsTabs(
            selectedTab = state.selectedTab,
            pendingCount = state.pendingCount,
            onSelectTab = onSelectTab,
        )

        HorizontalDivider(color = MaterialTheme.hack.gridLine, thickness = 1.dp)

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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RequestsTab.entries.forEach { tab ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                label = {
                    Text(
                        text = when (tab) {
                            RequestsTab.PENDING -> stringResource(R.string.requests_tab_pending, pendingCount)
                            RequestsTab.PROCESSED -> stringResource(R.string.requests_tab_processed)
                            RequestsTab.ALL -> stringResource(R.string.requests_tab_all)
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                shape = MaterialTheme.shapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
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
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    when (request.audience) {
                        RequestAudience.TEACHER -> R.string.requests_kind_publication
                        RequestAudience.ADMIN -> R.string.requests_kind_visit
                    },
                ),
                style = HackMonoTextStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RequestStatusBadge(status = request.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = request.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(8.dp))

        RequestInfoRow(
            icon = Icons.Default.Person,
            text = stringResource(
                R.string.requests_author_format,
                request.author.name,
                request.author.pupilClass,
            ),
        )

        RequestInfoRow(
            icon = Icons.Default.Schedule,
            text = stringResource(R.string.requests_date_format, request.proposedDate),
        )

        if (request is ReviewRequest.Visit) {
            RequestInfoRow(
                icon = Icons.Default.LocationOn,
                text = stringResource(R.string.requests_location_format, request.proposedLocation),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = request.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (request is ReviewRequest.Visit) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.requests_participants_title, request.participants.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = request.participants.joinToString { participant ->
                    participant.name + " · " + participant.pupilClass
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (request.status == ReviewStatus.APPROVED && request is ReviewRequest.Visit && request.confirmedDate != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.requests_confirmed_date_format, request.confirmedDate),
                    style = HackMonoTextStyle.copy(fontSize = 11.sp),
                    color = MaterialTheme.hack.success,
                )
                if (request.confirmedLocation != null) {
                    Text(
                        text = stringResource(R.string.requests_confirmed_location_format, request.confirmedLocation),
                        style = HackMonoTextStyle.copy(fontSize = 11.sp),
                        color = MaterialTheme.hack.success,
                    )
                }
            }
        }

        val declineReason = request.declineReason
        if (declineReason != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.requests_decline_reason_format, declineReason),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.hack.danger,
            )
        }

        if (request.isPending) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.hack.danger,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.requests_action_decline))
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.requests_action_approve))
                }
            }
        }
    }
}

@Composable
private fun RequestInfoRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RequestStatusBadge(status: ReviewStatus) {
    val containerColor = when (status) {
        ReviewStatus.PENDING_TEACHER, ReviewStatus.PENDING_ADMIN -> MaterialTheme.colorScheme.surfaceVariant
        ReviewStatus.APPROVED -> MaterialTheme.hack.success.copy(alpha = 0.16f)
        ReviewStatus.DECLINED -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (status) {
        ReviewStatus.PENDING_TEACHER, ReviewStatus.PENDING_ADMIN -> MaterialTheme.colorScheme.onSurfaceVariant
        ReviewStatus.APPROVED -> MaterialTheme.hack.success
        ReviewStatus.DECLINED -> MaterialTheme.colorScheme.onErrorContainer
    }

    Box(
        modifier = Modifier
            .background(containerColor, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(
                when (status) {
                    ReviewStatus.PENDING_TEACHER -> R.string.requests_status_pending_teacher
                    ReviewStatus.PENDING_ADMIN -> R.string.requests_status_pending_admin
                    ReviewStatus.APPROVED -> R.string.requests_status_approved
                    ReviewStatus.DECLINED -> R.string.requests_status_declined
                },
            ),
            style = HackMonoTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
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
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.requests_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(6.dp))
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
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.requests_access_denied_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.requests_access_denied_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RequestsDialog(
    dialog: RequestDialog,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onChangeDate: (String) -> Unit,
    onChangeLocation: (String) -> Unit,
    onChangeReason: (String) -> Unit,
    onConfirmApprove: () -> Unit,
    onConfirmDecline: () -> Unit,
) {
    when (dialog) {
        is RequestDialog.Approve -> AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) onDismiss()
            },
            title = {
                Text(stringResource(R.string.requests_dialog_approve_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = dialog.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                    if (dialog.requiresDetails) {
                        Text(
                            text = stringResource(R.string.requests_dialog_approve_visit_text),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedTextField(
                            value = dialog.confirmedDate,
                            onValueChange = onChangeDate,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.requests_confirm_date_label)) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                        )
                        OutlinedTextField(
                            value = dialog.confirmedLocation,
                            onValueChange = onChangeLocation,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.requests_confirm_location_label)) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.requests_dialog_approve_teacher_text),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmApprove,
                    enabled = !isSubmitting,
                ) {
                    Text(stringResource(R.string.requests_action_approve))
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
            title = {
                Text(stringResource(R.string.requests_dialog_decline_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = dialog.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = stringResource(R.string.requests_dialog_decline_text),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedTextField(
                        value = dialog.reason,
                        onValueChange = onChangeReason,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.requests_dialog_decline_reason_label)) },
                        placeholder = { Text(stringResource(R.string.requests_dialog_decline_reason_hint)) },
                        minLines = 2,
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDecline,
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.hack.danger),
                ) {
                    Text(stringResource(R.string.requests_action_decline))
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
