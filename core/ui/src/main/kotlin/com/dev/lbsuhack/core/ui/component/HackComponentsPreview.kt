package com.dev.lbsuhack.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.designsystem.theme.HackTheme

@Preview(
    name = "Shared UI · dark",
    group = "Shared UI",
    widthDp = 420,
    heightDp = 900,
    backgroundColor = 0xFF070A12,
    showBackground = true,
)
@Composable
private fun SharedUiDarkPreview() {
    HackTheme(mode = AppThemeMode.DARK) {
        SharedUiShowcase()
    }
}

@Preview(
    name = "Shared UI · light",
    group = "Shared UI",
    widthDp = 420,
    heightDp = 900,
    backgroundColor = 0xFFF4F6FB,
    showBackground = true,
)
@Composable
private fun SharedUiLightPreview() {
    HackTheme(mode = AppThemeMode.LIGHT) {
        SharedUiShowcase()
    }
}

@Composable
private fun SharedUiShowcase() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HackTopBar(
            title = "Shared UI",
            onBack = {},
            backContentDescription = "Back",
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HackSectionLabel("SECTION LABEL")
            HackFieldLabel("Field label")
            HackPrimaryButton(text = "Continue", onClick = {})
            HackPrimaryButton(text = "Loading", onClick = {}, loading = true)
            HackDotsLoader(color = MaterialTheme.colorScheme.primary)
            HackChanceMeter(title = "Chance to happen", percent = 58, levelText = "medium")
            HackChanceMeter(
                title = "Chance to happen",
                percent = 82,
                levelText = "high",
                prominent = true,
                description = "Similar events in this category usually happen.",
                footnote = "Estimated from past events of the same category",
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            ) {
                HackEmptyState(
                    title = "Nothing here yet",
                    modifier = Modifier.fillMaxSize(),
                    description = "Reusable empty state for any feature screen.",
                    icon = Icons.Default.Info,
                )
            }
        }
    }
}
