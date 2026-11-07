package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.ui.component.HackLoading
import com.dev.lbsuhack.core.ui.component.HackSnackbarHost
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.core.ui.component.showHackSnackbar
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.presentation.requests.RequestsViewModel
import com.dev.lbsuhack.feature.requests.store.requests.AccessState
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.ui.dialog.RequestsDialogHost
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun RequestsScreen(
    role: UserRole,
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit = {},
    viewModel: RequestsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.labels.collectLatest { label ->
            when (label) {
                is RequestsStore.Label.ShowMessage -> snackbarHostState.showHackSnackbar(label.text.resolve(context))
            }
        }
    }

    LifecycleResumeEffect(viewModel, role) {
        viewModel.accept(RequestsStore.Intent.Load(role))
        viewModel.accept(RequestsStore.Intent.StartPolling)
        onPauseOrDispose {
            viewModel.accept(RequestsStore.Intent.StopPolling)
            viewModel.accept(RequestsStore.Intent.Pause)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HackTopBar(
                title = stringResource(
                    when (role) {
                        UserRole.ORGANIZATOR -> R.string.requests_title_admin
                        else -> R.string.requests_title_teacher
                    },
                ),
                onBack = onBack,
                backContentDescription = stringResource(R.string.requests_back),
            )
        },
        snackbarHost = {
            HackSnackbarHost(hostState = snackbarHostState)
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val audience = state.audience
            when {
                state.accessState == AccessState.Checking || state.isInitialLoading -> {
                    HackLoading(modifier = Modifier.fillMaxSize())
                }

                state.accessState == AccessState.Denied -> {
                    RequestsAccessDenied(modifier = Modifier.fillMaxSize())
                }

                state.loadFailed && state.requests.isEmpty() -> {
                    RequestsLoadErrorState(
                        onRetry = { viewModel.accept(RequestsStore.Intent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                audience != null -> {
                    RequestsContent(
                        audience = audience,
                        state = state,
                        onApprove = { viewModel.accept(RequestsStore.Intent.ClickApprove(it)) },
                        onDecline = { viewModel.accept(RequestsStore.Intent.ClickDecline(it)) },
                        onOpen = onOpenActivity,
                        onRefresh = { viewModel.accept(RequestsStore.Intent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

        }
    }

    RequestsDialogHost(
        dialog = state.dialog,
        isSubmitting = state.isSubmitting,
        onDismiss = { viewModel.accept(RequestsStore.Intent.DismissDialog) },
        onChangeReason = { viewModel.accept(RequestsStore.Intent.ChangeDeclineReason(it)) },
        onConfirmDecline = { viewModel.accept(RequestsStore.Intent.ConfirmDecline) },
    )
}
