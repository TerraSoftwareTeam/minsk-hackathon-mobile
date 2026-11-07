package com.dev.lbsuhack.core.ui.component

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.designsystem.theme.HackTheme

@Composable
internal fun HackSnackbar(data: SnackbarData, modifier: Modifier = Modifier) {
    Snackbar(
        snackbarData = data,
        actionOnNewLine = data.visuals.actionLabel != null,
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        actionColor = MaterialTheme.colorScheme.onSurface,
        dismissActionContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(widthDp = 320)
@Composable
private fun SnackbarDarkPreview() {
    HackTheme(mode = AppThemeMode.DARK) {
        HackSnackbar(data = PreviewSnackbarData)
    }
}

@Preview(widthDp = 320)
@Composable
private fun SnackbarLightPreview() {
    HackTheme(mode = AppThemeMode.LIGHT) {
        HackSnackbar(data = PreviewSnackbarData)
    }
}

private object PreviewSnackbarData : SnackbarData {
    override val visuals = object : SnackbarVisuals {
        override val message = "Не удалось загрузить данные. Проверьте подключение к интернету."
        override val actionLabel = "Повторить"
        override val withDismissAction = true
        override val duration = SnackbarDuration.Short
    }

    override fun dismiss() = Unit
    override fun performAction() = Unit
}
