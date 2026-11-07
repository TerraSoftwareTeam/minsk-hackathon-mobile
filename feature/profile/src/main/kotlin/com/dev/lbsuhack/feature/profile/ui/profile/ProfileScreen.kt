package com.dev.lbsuhack.feature.profile.ui.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.localization.text.resolve
import com.dev.lbsuhack.core.ui.component.HackLoading
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.presentation.profile.ProfileViewModel
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStore
import com.dev.lbsuhack.feature.profile.ui.profile.component.ProfileErrorState
import com.dev.lbsuhack.feature.profile.ui.profile.dialog.EditProfileDialog
import com.dev.lbsuhack.feature.profile.ui.profile.dialog.LogoutConfirmDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onBack: (() -> Unit)?,
    onOpenSettings: () -> Unit,
    onOpenRequests: (UserRole) -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var isLogoutDialogVisible by rememberSaveable { mutableStateOf(false) }

    LifecycleResumeEffect(viewModel) {
        viewModel.accept(ProfileStore.Intent.Load)
        onPauseOrDispose {}
    }

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                is ProfileStore.Label.ShowMessage -> snackbarHostState.showSnackbar(label.text.resolve(context))
            }
        }
    }

    if (isLogoutDialogVisible) {
        LogoutConfirmDialog(
            onConfirm = {
                isLogoutDialogVisible = false
                viewModel.accept(ProfileStore.Intent.Logout)
            },
            onDismiss = { isLogoutDialogVisible = false },
        )
    }

    val editDialog = state.dialog
    if (editDialog != null) {
        EditProfileDialog(
            dialog = editDialog,
            isSaving = state.isSaving,
            onDismiss = { viewModel.accept(ProfileStore.Intent.DismissDialog) },
            onChangeName = { viewModel.accept(ProfileStore.Intent.ChangeName(it)) },
            onChangeSurname = { viewModel.accept(ProfileStore.Intent.ChangeSurname(it)) },
            onConfirm = { viewModel.accept(ProfileStore.Intent.ConfirmEdit) },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HackTopBar(
                title = stringResource(R.string.profile_title),
                onBack = onBack,
                backContentDescription = stringResource(R.string.profile_back),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { padding ->
        when {
            state.isLoading && !state.hasUser -> {
                HackLoading(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            state.isError && !state.hasUser -> {
                ProfileErrorState(
                    onRetry = { viewModel.accept(ProfileStore.Intent.Load) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            state.hasUser -> {
                ProfileContent(
                    state = state,
                    onEditClick = { viewModel.accept(ProfileStore.Intent.ClickEdit) },
                    onOpenRequests = onOpenRequests,
                    onToggleNotifications = { viewModel.accept(ProfileStore.Intent.ToggleNotifications) },
                    onOpenSettings = onOpenSettings,
                    onLogoutClick = { isLogoutDialogVisible = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }
        }
    }
}
