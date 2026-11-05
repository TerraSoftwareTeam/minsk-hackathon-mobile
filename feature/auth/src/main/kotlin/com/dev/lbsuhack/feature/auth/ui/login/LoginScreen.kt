package com.dev.lbsuhack.feature.auth.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.presentation.login.LoginViewModel
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.ui.AuthBrandHero
import com.dev.lbsuhack.feature.auth.ui.AuthDemoBanner
import com.dev.lbsuhack.feature.auth.ui.AuthError
import com.dev.lbsuhack.feature.auth.ui.AuthPasswordField
import com.dev.lbsuhack.feature.auth.ui.AuthPrimaryButton
import com.dev.lbsuhack.feature.auth.ui.AuthRoleSelector
import com.dev.lbsuhack.feature.auth.ui.AuthSwitchPrompt
import com.dev.lbsuhack.feature.auth.ui.AuthTextField
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onOpenRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                LoginStore.Label.OpenRegister -> onOpenRegister()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.hack.glow.copy(alpha = 0.4f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                AuthBrandHero(
                    title = stringResource(R.string.auth_login_title),
                    subtitle = stringResource(R.string.auth_login_subtitle),
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.auth_login_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        AuthRoleSelector(
                            selectedRole = state.role,
                            onRoleSelected = { viewModel.accept(LoginStore.Intent.RoleChanged(it)) },
                        )

                        AuthTextField(
                            value = state.username,
                            onValueChange = { viewModel.accept(LoginStore.Intent.UsernameChanged(it)) },
                            label = stringResource(R.string.auth_username),
                            placeholder = stringResource(R.string.auth_username_placeholder),
                            leadingIcon = Icons.Rounded.Person,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        )

                        AuthPasswordField(
                            value = state.password,
                            onValueChange = { viewModel.accept(LoginStore.Intent.PasswordChanged(it)) },
                            imeAction = ImeAction.Done,
                            onImeAction = { viewModel.accept(LoginStore.Intent.Submit) },
                        )

                        AuthError(error = state.error)

                        AuthPrimaryButton(
                            text = stringResource(R.string.auth_login_action),
                            isLoading = state.isLoading,
                            onClick = { viewModel.accept(LoginStore.Intent.Submit) },
                        )
                    }
                }

                AuthDemoBanner()

                AuthSwitchPrompt(
                    promptText = stringResource(R.string.auth_no_account_prompt),
                    actionText = stringResource(R.string.auth_open_register),
                    onActionClick = { viewModel.accept(LoginStore.Intent.OpenRegister) },
                    enabled = !state.isLoading,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
