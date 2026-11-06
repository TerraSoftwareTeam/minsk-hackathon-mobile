package com.dev.lbsuhack.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.designsystem.theme.HackTheme

@Composable
fun HackAvatar(
    name: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = onClick != null, onClick = onClick ?: {}),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Preview
@Composable
private fun HackAvatarPreviewDark() {
    HackTheme(mode = AppThemeMode.DARK) {
        HackAvatar(name = "Никита Жевнов")
    }
}

@Preview
@Composable
private fun HackAvatarPreviewLight() {
    HackTheme(mode = AppThemeMode.LIGHT) {
        HackAvatar(name = "Никита Жевнов")
    }
}
