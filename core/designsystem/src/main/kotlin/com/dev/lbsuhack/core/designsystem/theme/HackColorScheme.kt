package com.dev.lbsuhack.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class HackExtras(
    val glow: Color,
    val danger: Color,
    val success: Color,
    val gridLine: Color,
)

val DarkHackColorScheme = darkColorScheme(
    primary = Color(0xFF1D74F5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF193259),
    onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFF3882F6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF22242C),
    onSecondaryContainer = Color(0xFFD8E4FF),
    tertiary = Color(0xFFFF9F1C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF3D2700),
    onTertiaryContainer = Color(0xFFFFDFA8),
    background = Color(0xFF16171B),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF202126),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF27282F),
    onSurfaceVariant = Color(0xFF8E9099),
    surfaceTint = Color(0xFF1D74F5),
    surfaceBright = Color(0xFF2C2D35),
    surfaceDim = Color(0xFF16171B),
    surfaceContainerLowest = Color(0xFF101114),
    surfaceContainerLow = Color(0xFF1B1C21),
    surfaceContainer = Color(0xFF202126),
    surfaceContainerHigh = Color(0xFF27282F),
    surfaceContainerHighest = Color(0xFF30313A),
    outline = Color(0xFF383A44),
    outlineVariant = Color(0xFF282932),
    error = Color(0xFFFF4757),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF3E151B),
    onErrorContainer = Color(0xFFFFD9DE),
    scrim = Color(0xFF000000),
)

val LightHackColorScheme = lightColorScheme(
    primary = Color(0xFF2E5BFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE4FF),
    onPrimaryContainer = Color(0xFF0A1B4D),
    secondary = Color(0xFF00897B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC7F2EC),
    onSecondaryContainer = Color(0xFF00201B),
    tertiary = Color(0xFF9A6100),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE7C2),
    onTertiaryContainer = Color(0xFF2A1A00),
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF0B1020),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0B1020),
    surfaceVariant = Color(0xFFE8EDF7),
    onSurfaceVariant = Color(0xFF5A6680),
    surfaceTint = Color(0xFF2E5BFF),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE3E8F2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFE),
    surfaceContainer = Color(0xFFF1F4FB),
    surfaceContainerHigh = Color(0xFFEAEFF8),
    surfaceContainerHighest = Color(0xFFE3E9F4),
    outline = Color(0xFFC3CCE0),
    outlineVariant = Color(0xFFDCE3F0),
    error = Color(0xFFD62A45),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDADF),
    onErrorContainer = Color(0xFF3F0011),
    scrim = Color(0xFF000000),
)

val DarkHackExtras = HackExtras(
    glow = Color(0x331D74F5),
    danger = Color(0xFFFF4757),
    success = Color(0xFF22C55E),
    gridLine = Color(0xFF26272F),
)

val LightHackExtras = HackExtras(
    glow = Color(0x1F2E5BFF),
    danger = Color(0xFFD62A45),
    success = Color(0xFF0F8A5F),
    gridLine = Color(0xFFDCE3F0),
)
