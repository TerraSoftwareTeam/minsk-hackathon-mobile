package com.dev.lbsuhack.feature.profile.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.designsystem.mode.ThemeController
import com.dev.lbsuhack.core.localization.language.LanguageController
import com.dev.lbsuhack.core.ui.component.HackSectionLabel
import com.dev.lbsuhack.core.ui.component.HackTopBar
import com.dev.lbsuhack.feature.profile.R
import com.dev.lbsuhack.feature.profile.presentation.settings.SettingsViewModel
import com.dev.lbsuhack.feature.profile.store.settings.SettingsStore
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    languageController: LanguageController = koinInject(),
    themeController: ThemeController = koinInject(),
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
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            SettingsSection(
                title = stringResource(R.string.profile_theme),
                description = stringResource(R.string.profile_theme_description),
            ) {
                themeController.availableModes.forEachIndexed { index, mode ->
                    if (index > 0) SettingsDivider()
                    SettingsOption(
                        selected = state.theme == mode,
                        label = stringResource(mode.labelRes()),
                        icon = when (mode) {
                            AppThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness
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
                languageController.availableLanguages.forEachIndexed { index, language ->
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

@Composable
private fun SettingsSection(title: String, description: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            HackSectionLabel(text = title)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge)
                .selectableGroup(),
            content = content,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SettingsOption(selected: Boolean, label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.large)
            .background(if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = MaterialTheme.shapes.large,
            )
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun AppThemeMode.labelRes(): Int = when (this) {
    AppThemeMode.SYSTEM -> R.string.profile_theme_system
    AppThemeMode.LIGHT -> R.string.profile_theme_light
    AppThemeMode.DARK -> R.string.profile_theme_dark
}
