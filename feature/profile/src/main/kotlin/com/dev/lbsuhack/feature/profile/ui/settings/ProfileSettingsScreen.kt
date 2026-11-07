package com.dev.lbsuhack.feature.profile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.presentation.settings.SettingsViewModel
import com.dev.lbsuhack.feature.profile.store.settings.SettingsStore
import com.dev.lbsuhack.feature.profile.ui.settings.component.SettingsDivider
import com.dev.lbsuhack.feature.profile.ui.settings.component.SettingsOption
import com.dev.lbsuhack.feature.profile.ui.settings.component.SettingsSection
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HackTopBar(
                title = stringResource(R.string.profile_settings_title),
                onBack = onBack,
                backContentDescription = stringResource(R.string.profile_back),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            SettingsSection(
                title = stringResource(R.string.profile_theme),
                description = stringResource(R.string.profile_theme_description),
            ) {
                state.availableModes.forEachIndexed { index, mode ->
                    if (index > 0) SettingsDivider()
                    SettingsOption(
                        selected = state.theme == mode,
                        label = stringResource(mode.labelRes()),
                        icon = when (mode) {
                            AppThemeMode.LIGHT -> Icons.Outlined.LightMode
                            AppThemeMode.DARK -> Icons.Outlined.DarkMode
                        },
                        onClick = { viewModel.accept(SettingsStore.Intent.SetTheme(mode)) },
                    )
                }
            }
            SettingsSection(
                title = stringResource(R.string.profile_language),
                description = stringResource(R.string.profile_language_description),
            ) {
                state.availableLanguages.forEachIndexed { index, language ->
                    if (index > 0) SettingsDivider()
                    SettingsOption(
                        selected = state.language == language,
                        label = stringResource(language.labelRes),
                        icon = Icons.Outlined.Language,
                        onClick = { viewModel.accept(SettingsStore.Intent.SetLanguage(language)) },
                    )
                }
            }
        }
    }
}
