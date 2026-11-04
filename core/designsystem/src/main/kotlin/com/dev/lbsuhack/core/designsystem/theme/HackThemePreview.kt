package com.dev.lbsuhack.core.designsystem.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider

@Preview(
    name = "Design system · dark",
    group = "Design system",
    widthDp = 420,
    heightDp = 980,
    backgroundColor = 0xFF070A12,
    showBackground = true,
)
@Composable
private fun DesignSystemDarkPreview() {
    MaterialTheme(
        colorScheme = DarkHackColorScheme,
        typography = HackTypography,
        shapes = HackShapes,
    ) {
        CompositionLocalProvider(LocalHackExtras provides DarkHackExtras) {
            DesignSystemShowcase(subtitle = "Dark · brand default")
        }
    }
}

@Preview(
    name = "Design system · light",
    group = "Design system",
    widthDp = 420,
    heightDp = 980,
    backgroundColor = 0xFFF4F6FB,
    showBackground = true,
)
@Composable
private fun DesignSystemLightPreview() {
    MaterialTheme(
        colorScheme = LightHackColorScheme,
        typography = HackTypography,
        shapes = HackShapes,
    ) {
        CompositionLocalProvider(LocalHackExtras provides LightHackExtras) {
            DesignSystemShowcase(subtitle = "Light · alternate")
        }
    }
}

@Composable
private fun DesignSystemShowcase(subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandBar()
            ShowcaseHeader(subtitle)
            TimerPanel()

            Section("METRICS") { StatsRow() }
            Section("ACTIONS") { ActionRow() }
            Section("TAGS") { ChipsRow() }
            Section("PALETTE") { PaletteRow() }
            Section("TYPE") { TypeScale() }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = "Rounded 10–20 dp · no dynamic color · system bars synced",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BrandBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                    ),
                ),
                shape = RoundedCornerShape(50),
            ),
    )
}

@Composable
private fun ShowcaseHeader(subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "DESIGN SYSTEM",
            style = HackMonoTextStyle,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Build. Ship. Win.",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimerPanel() {
    Panel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "HACKATHON 2026",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusPill(text = "LIVE", color = MaterialTheme.hack.success)
        }

        Text(
            text = "04:12:00",
            style = HackTimerTextStyle,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Text(
            text = "Submission closes in",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LinearProgressIndicator(
            progress = { 0.68f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "68% submitted",
                style = HackMonoTextStyle,
                color = MaterialTheme.hack.success,
            )
            Text(
                text = "Team Alpha",
                style = HackMonoTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatTile("RANK", "#3", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        StatTile("TEAM", "4/4", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
        StatTile("XP", "1280", MaterialTheme.hack.success, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = HackMonoTextStyle.copy(fontWeight = FontWeight.Bold),
                color = accent,
            )
        }
    }
}

@Composable
private fun ActionRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(onClick = {}, modifier = Modifier.weight(1f)) {
            Text("Submit project")
        }
        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
            Text("View rules")
        }
    }
}

@Composable
private fun ChipsRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = {}, label = { Text("Kotlin") })
        AssistChip(onClick = {}, label = { Text("Compose") })
        AssistChip(onClick = {}, label = { Text("Navigation 3") })
    }
}

@Composable
private fun PaletteRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Swatch("Primary", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        Swatch("Secondary", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
        Swatch("Tertiary", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
        Swatch("Error", MaterialTheme.colorScheme.error, Modifier.weight(1f))
        Swatch("Success", MaterialTheme.hack.success, Modifier.weight(1f))
    }
}

@Composable
private fun Swatch(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(color = color, shape = RoundedCornerShape(10.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(10.dp),
                ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun TypeScale() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Display", style = MaterialTheme.typography.displaySmall)
        Text("Headline", style = MaterialTheme.typography.headlineSmall)
        Text("Title", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Body copy for descriptions and hints.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "LABEL · MONO VALUE 04:12",
            style = HackMonoTextStyle,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
