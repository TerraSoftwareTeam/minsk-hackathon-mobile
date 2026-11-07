package com.dev.lbsuhack.feature.requests.ui.requests

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.presentation.requests.RequestsViewModel
import com.dev.lbsuhack.feature.requests.store.requests.AccessState
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.ui.dialog.RequestsDialogHost
import com.dev.lbsuhack.feature.requests.ui.labels.titleLabelRes
import org.koin.androidx.compose.koinViewModel

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

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                is RequestsStore.Label.ShowMessage -> snackbarHostState.showSnackbar(label.text.resolve(context))
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
            RequestsTopBar(
                title = stringResource(state.audience?.titleLabelRes() ?: R.string.requests_title),
                isRefreshing = state.isRefreshing,
                onBack = onBack,
                onRefresh = { viewModel.accept(RequestsStore.Intent.Refresh) },
                onOpenProfile = onOpenProfile,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { padding ->
        val audience = state.audience
        when {
            state.accessState == AccessState.Checking -> {
                HackLoading(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            state.accessState == AccessState.Denied -> {
                RequestsAccessDenied(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            state.isInitialLoading -> {
                HackLoading(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            state.loadFailed && state.requests.isEmpty() -> {
                RequestsLoadErrorState(
                    onRetry = { viewModel.accept(RequestsStore.Intent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            audience != null -> {
                RequestsContent(
                    audience = audience,
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onSelectTab = { viewModel.accept(RequestsStore.Intent.SelectTab(it)) },
                    onApprove = { viewModel.accept(RequestsStore.Intent.ClickApprove(it)) },
                    onDecline = { viewModel.accept(RequestsStore.Intent.ClickDecline(it)) },
                    onRefresh = { viewModel.accept(RequestsStore.Intent.Refresh) },
                )
            }
        }
    }

    RequestsDialogHost(
        dialog = state.dialog,
        audience = state.audience,
        isSubmitting = state.isSubmitting,
        onDismiss = { viewModel.accept(RequestsStore.Intent.DismissDialog) },
        onChangeReason = { viewModel.accept(RequestsStore.Intent.ChangeDeclineReason(it)) },
        onChangeStartsAt = { viewModel.accept(RequestsStore.Intent.ChangeStartsAt(it)) },
        onConfirmApprove = { viewModel.accept(RequestsStore.Intent.ConfirmApprove) },
        onConfirmDecline = { viewModel.accept(RequestsStore.Intent.ConfirmDecline) },
    )
}
