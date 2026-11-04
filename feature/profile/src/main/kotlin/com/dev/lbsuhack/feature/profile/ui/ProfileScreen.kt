package com.dev.lbsuhack.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.AppThemeMode
import com.dev.lbsuhack.core.designsystem.theme.ThemeController
import com.dev.lbsuhack.core.localization.LanguageController
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.presentation.ProfileViewModel
import com.dev.lbsuhack.feature.profile.store.ProfileStore
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    languageController: LanguageController = koinInject(),
    themeController: ThemeController = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentLanguage by languageController.current.collectAsStateWithLifecycle()
    val currentTheme by themeController.current.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.labels.collect { label ->
            when (label) {
                is ProfileStore.Label.NotificationToggled -> {
                    snackbarHostState.showSnackbar(
                        context.getString(
                            if (label.enabled) {
                                R.string.profile_notifications_enabled
                            } else {
                                R.string.profile_notifications_disabled
                            }
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.profile_title)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Column {
                        Text(
                            text = state.username,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = state.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.profile_notifications),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.profile_notifications_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = { viewModel.accept(ProfileStore.Intent.ToggleNotifications) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pluralStringResource(
                            R.plurals.profile_saved_items,
                            state.savedItemsCount,
                            state.savedItemsCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_language),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.profile_language_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    languageController.availableLanguages.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentLanguage == language,
                                    onClick = { languageController.setLanguage(language) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = currentLanguage == language,
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = stringResource(language.labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_theme),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.profile_theme_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    themeController.availableModes.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentTheme == mode,
                                    onClick = { themeController.setMode(mode) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = currentTheme == mode,
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = stringResource(mode.labelRes()),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.accept(ProfileStore.Intent.IncrementSaved) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_add_saved))
            }
        }
    }
}

private fun AppThemeMode.labelRes(): Int = when (this) {
    AppThemeMode.SYSTEM -> R.string.profile_theme_system
    AppThemeMode.LIGHT -> R.string.profile_theme_light
    AppThemeMode.DARK -> R.string.profile_theme_dark
}
