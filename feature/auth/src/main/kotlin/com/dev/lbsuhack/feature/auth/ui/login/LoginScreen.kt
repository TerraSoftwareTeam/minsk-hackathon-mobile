package com.dev.lbsuhack.feature.auth.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.presentation.login.LoginViewModel
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.ui.login.component.LoginCredentialsContent
import com.dev.lbsuhack.feature.auth.ui.login.component.LoginWelcomeContent
import org.koin.androidx.compose.koinViewModel

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
