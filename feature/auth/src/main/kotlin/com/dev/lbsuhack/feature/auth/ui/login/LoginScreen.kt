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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.presentation.login.LoginViewModel
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.ui.AuthClassPicker
import com.dev.lbsuhack.feature.auth.ui.AuthDemoBanner
import com.dev.lbsuhack.feature.auth.ui.AuthError
import com.dev.lbsuhack.feature.auth.ui.AuthPasswordField
import com.dev.lbsuhack.feature.auth.ui.AuthPrimaryButton
import com.dev.lbsuhack.feature.auth.ui.AuthTextField
import org.koin.androidx.compose.koinViewModel

private enum class LoginStep {
    WELCOME,
    CREDENTIALS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableStateOf(LoginStep.WELCOME) }

    if (step == LoginStep.CREDENTIALS) {
        BackHandler {
            step = LoginStep.WELCOME
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (step == LoginStep.CREDENTIALS) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (state.role) {
                                UserRole.TEACHER -> stringResource(R.string.auth_login_teacher_title)
                                UserRole.ORGANIZATOR -> stringResource(R.string.auth_login_organizer_title)
                                else -> stringResource(R.string.auth_login_pupil_title)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { step = LoginStep.WELCOME }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.auth_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        },
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
                        onSelectOrganizer = {
                            viewModel.accept(LoginStore.Intent.RoleChanged(UserRole.ORGANIZATOR))
                            step = LoginStep.CREDENTIALS
                        },
                    )
                }
                LoginStep.CREDENTIALS -> {
                    LoginCredentialsContent(
                        state = state,
                        onUsernameChange = { viewModel.accept(LoginStore.Intent.UsernameChanged(it)) },
                        onPasswordChange = { viewModel.accept(LoginStore.Intent.PasswordChanged(it)) },
                        onSurnameChange = { viewModel.accept(LoginStore.Intent.SurnameChanged(it)) },
                        onNameChange = { viewModel.accept(LoginStore.Intent.NameChanged(it)) },
                        onClassNameChange = { viewModel.accept(LoginStore.Intent.ClassNameChanged(it)) },
                        onSubmit = { viewModel.accept(LoginStore.Intent.Submit) },
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
    onSelectOrganizer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brandText = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
            append("ko")
        }
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("la")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = brandText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2.5).sp,
                ),
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.auth_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    letterSpacing = (-0.2).sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1.2f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Button(
                onClick = onSelectPupil,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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

            Surface(
                onClick = onSelectOrganizer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.auth_role_organizator_action),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginCredentialsContent(
    state: LoginStore.State,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onClassNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        when (state.role) {
            UserRole.ORGANIZATOR -> Text(
                text = stringResource(R.string.auth_login_organizer_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            UserRole.TEACHER -> {
                AuthTextField(
                    value = state.surname,
                    onValueChange = onSurnameChange,
                    label = stringResource(R.string.auth_surname),
                    placeholder = stringResource(R.string.auth_surname_placeholder),
                    leadingIcon = Icons.Rounded.Badge,
                    imeAction = ImeAction.Next,
                )

                AuthTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.auth_name),
                    placeholder = stringResource(R.string.auth_name_placeholder),
                    leadingIcon = Icons.Rounded.Person,
                    imeAction = ImeAction.Done,
                )

                AuthClassPicker(
                    value = state.className,
                    onValueChange = onClassNameChange,
                )
            }

            UserRole.PUPIL -> {
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
            }
        }

        AuthError(error = state.error)

        Spacer(modifier = Modifier.height(4.dp))

        AuthPrimaryButton(
            text = when (state.role) {
                UserRole.ORGANIZATOR -> stringResource(R.string.auth_login_action_organizer)
                UserRole.TEACHER -> stringResource(R.string.auth_login_action_teacher)
                UserRole.PUPIL -> stringResource(R.string.auth_login_action)
            },
            isLoading = state.isLoading,
            onClick = onSubmit,
        )

        if (state.role == UserRole.PUPIL) {
            AuthDemoBanner()
        }
    }
}
