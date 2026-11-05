package com.dev.lbsuhack.feature.auth.ui.register

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.presentation.register.RegisterViewModel
import com.dev.lbsuhack.feature.auth.store.register.RegisterStore
import com.dev.lbsuhack.feature.auth.ui.AuthBrandHero
import com.dev.lbsuhack.feature.auth.ui.AuthDemoBanner
import com.dev.lbsuhack.feature.auth.ui.AuthError
import com.dev.lbsuhack.feature.auth.ui.AuthPasswordField
import com.dev.lbsuhack.feature.auth.ui.AuthPrimaryButton
import com.dev.lbsuhack.feature.auth.ui.AuthSwitchPrompt
import com.dev.lbsuhack.feature.auth.ui.AuthTextField
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                RegisterStore.Label.BackToLogin -> onBack()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.auth_register_top_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.accept(RegisterStore.Intent.BackToLogin) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.auth_back),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.hack.glow,
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                AuthBrandHero(
                    title = stringResource(R.string.auth_register_title),
                    subtitle = stringResource(R.string.auth_register_subtitle),
                    showBadges = false,
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.auth_register_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        AuthTextField(
                            value = state.name,
                            onValueChange = { viewModel.accept(RegisterStore.Intent.NameChanged(it)) },
                            label = stringResource(R.string.auth_name),
                            placeholder = stringResource(R.string.auth_name_placeholder),
                            leadingIcon = Icons.Rounded.Person,
                            imeAction = ImeAction.Next,
                        )

                        AuthTextField(
                            value = state.email,
                            onValueChange = { viewModel.accept(RegisterStore.Intent.EmailChanged(it)) },
                            label = stringResource(R.string.auth_email),
                            placeholder = stringResource(R.string.auth_email_placeholder),
                            leadingIcon = Icons.Rounded.AlternateEmail,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        )

                        AuthPasswordField(
                            value = state.password,
                            onValueChange = { viewModel.accept(RegisterStore.Intent.PasswordChanged(it)) },
                            imeAction = ImeAction.Done,
                            onImeAction = { viewModel.accept(RegisterStore.Intent.Submit) },
                        )

                        AuthError(error = state.error)

                        AuthPrimaryButton(
                            text = stringResource(R.string.auth_register_action),
                            isLoading = state.isLoading,
                            onClick = { viewModel.accept(RegisterStore.Intent.Submit) },
                        )
                    }
                }

                AuthDemoBanner()

                AuthSwitchPrompt(
                    promptText = stringResource(R.string.auth_has_account_prompt),
                    actionText = stringResource(R.string.auth_open_login),
                    onActionClick = { viewModel.accept(RegisterStore.Intent.BackToLogin) },
                    enabled = !state.isLoading,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
