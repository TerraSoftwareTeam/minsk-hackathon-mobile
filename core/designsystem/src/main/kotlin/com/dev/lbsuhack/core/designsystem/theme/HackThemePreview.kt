package com.dev.lbsuhack.core.designsystem.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Hack dark", backgroundColor = 0xFF070A12, showBackground = true)
@Composable
private fun HackThemeDarkPreview() {
    HackTheme(mode = AppThemeMode.DARK) {
        ThemePreviewContent()
    }
}

@Preview(name = "Hack light", backgroundColor = 0xFFF4F6FB, showBackground = true)
@Composable
private fun HackThemeLightPreview() {
    HackTheme(mode = AppThemeMode.LIGHT) {
        ThemePreviewContent()
    }
}

@Composable
private fun ThemePreviewContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Lbsuhack", style = MaterialTheme.typography.displaySmall)
            Text(
                text = "DEADLINE 04:12:00",
                style = HackTimerTextStyle,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "Team Alpha", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "RANK #3",
                        style = HackMonoTextStyle,
                        color = MaterialTheme.hack.success,
                    )
                }
            }
        }
    }
}
