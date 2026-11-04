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
    primary = Color(0xFF4F7DFF),
    onPrimary = Color(0xFF05070F),
    primaryContainer = Color(0xFF1B2A5E),
    onPrimaryContainer = Color(0xFFD8E1FF),
    secondary = Color(0xFF00E5C7),
    onSecondary = Color(0xFF00201B),
    secondaryContainer = Color(0xFF083F38),
    onSecondaryContainer = Color(0xFFB2FFF4),
    tertiary = Color(0xFFFFB627),
    onTertiary = Color(0xFF2A1A00),
    tertiaryContainer = Color(0xFF4A3200),
    onTertiaryContainer = Color(0xFFFFE0A6),
    background = Color(0xFF070A12),
    onBackground = Color(0xFFF2F5FF),
    surface = Color(0xFF101623),
    onSurface = Color(0xFFF2F5FF),
    surfaceVariant = Color(0xFF161D2E),
    onSurfaceVariant = Color(0xFF9AA6C2),
    surfaceTint = Color(0xFF4F7DFF),
    surfaceBright = Color(0xFF1A2235),
    surfaceDim = Color(0xFF070A12),
    surfaceContainerLowest = Color(0xFF04060C),
    surfaceContainerLow = Color(0xFF0C111C),
    surfaceContainer = Color(0xFF101623),
    surfaceContainerHigh = Color(0xFF161D2E),
    surfaceContainerHighest = Color(0xFF1E2739),
    outline = Color(0xFF2A3550),
    outlineVariant = Color(0xFF1C2537),
    error = Color(0xFFFF5470),
    onError = Color(0xFF2B0009),
    errorContainer = Color(0xFF5A1524),
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
    glow = Color(0x4D4F7DFF),
    danger = Color(0xFFFF5470),
    success = Color(0xFF3DDC97),
    gridLine = Color(0xFF1C2537),
)

val LightHackExtras = HackExtras(
    glow = Color(0x1F2E5BFF),
    danger = Color(0xFFD62A45),
    success = Color(0xFF0F8A5F),
    gridLine = Color(0xFFDCE3F0),
)
