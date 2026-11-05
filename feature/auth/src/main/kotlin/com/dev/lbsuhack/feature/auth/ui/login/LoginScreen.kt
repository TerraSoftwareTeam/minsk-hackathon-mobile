package com.dev.lbsuhack.feature.auth.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.presentation.login.LoginViewModel
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.ui.AuthError
import com.dev.lbsuhack.feature.auth.ui.AuthPasswordField
import com.dev.lbsuhack.feature.auth.ui.AuthPrimaryButton
import com.dev.lbsuhack.feature.auth.ui.AuthSwitchPrompt
import com.dev.lbsuhack.feature.auth.ui.AuthTextField
import org.koin.androidx.compose.koinViewModel

private enum class LoginStep {
    WELCOME,
    CREDENTIALS,
}

@Composable
fun LoginScreen(
    onOpenRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableStateOf(LoginStep.WELCOME) }

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                LoginStore.Label.OpenRegister -> onOpenRegister()
            }
        }
    }

    if (step == LoginStep.CREDENTIALS) {
        BackHandler {
            step = LoginStep.WELCOME
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        AnimatedContent(
            targetState = step,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            transitionSpec = {
                if (targetState == LoginStep.CREDENTIALS) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "loginStepTransition",
        ) { currentStep ->
            when (currentStep) {
                LoginStep.WELCOME -> {
                    LoginWelcomeContent(
                        onSelectPupil = {
                            viewModel.accept(LoginStore.Intent.RoleChanged(UserRole.PUPIL))
                            step = LoginStep.CREDENTIALS
                        },
                        onSelectTeacher = {
                            viewModel.accept(LoginStore.Intent.RoleChanged(UserRole.TEACHER))
                            step = LoginStep.CREDENTIALS
                        },
                        onOpenRegister = { viewModel.accept(LoginStore.Intent.OpenRegister) },
                    )
                }
                LoginStep.CREDENTIALS -> {
                    LoginCredentialsContent(
                        state = state,
                        onBackClick = { step = LoginStep.WELCOME },
                        onUsernameChange = { viewModel.accept(LoginStore.Intent.UsernameChanged(it)) },
                        onPasswordChange = { viewModel.accept(LoginStore.Intent.PasswordChanged(it)) },
                        onSubmit = { viewModel.accept(LoginStore.Intent.Submit) },
                        onOpenRegister = { viewModel.accept(LoginStore.Intent.OpenRegister) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginWelcomeContent(
    onSelectPupil: () -> Unit,
    onSelectTeacher: () -> Unit,
    onOpenRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.auth_brand),
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.auth_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 24.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onSelectPupil,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.auth_role_pupil_action),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Surface(
                onClick = onSelectTeacher,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.auth_role_teacher_action),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            AuthSwitchPrompt(
                promptText = stringResource(R.string.auth_no_account_prompt),
                actionText = stringResource(R.string.auth_open_register),
                onActionClick = onOpenRegister,
            )
        }
    }
}

@Composable
private fun LoginCredentialsContent(
    state: LoginStore.State,
    onBackClick: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.auth_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            text = if (state.role == UserRole.TEACHER) {
                stringResource(R.string.auth_login_teacher_title)
            } else {
                stringResource(R.string.auth_login_pupil_title)
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(4.dp))

        AuthTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            label = stringResource(R.string.auth_username),
            placeholder = stringResource(R.string.auth_username_placeholder),
            leadingIcon = Icons.Rounded.Person,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
        )

        AuthPasswordField(
            value = state.password,
            onValueChange = onPasswordChange,
            imeAction = ImeAction.Done,
            onImeAction = onSubmit,
        )

        AuthError(error = state.error)

        Spacer(modifier = Modifier.height(8.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_login_action),
            isLoading = state.isLoading,
            onClick = onSubmit,
        )

        AuthSwitchPrompt(
            promptText = stringResource(R.string.auth_no_account_prompt),
            actionText = stringResource(R.string.auth_open_register),
            onActionClick = onOpenRegister,
            enabled = !state.isLoading,
        )
    }
}

