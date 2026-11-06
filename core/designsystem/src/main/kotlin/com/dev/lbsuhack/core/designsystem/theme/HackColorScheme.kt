package com.dev.lbsuhack.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class HackAccent(
    val accent: Color,
    val container: Color,
)

@Immutable
data class HackExtras(
    val glow: Color,
    val danger: Color,
    val success: Color,
    val successContainer: Color,
    val gridLine: Color,
    val violet: HackAccent,
    val amber: HackAccent,
    val green: HackAccent,
    val pink: HackAccent,
)

val DarkHackColorScheme = darkColorScheme(
    primary = Color(0xFF2E5BFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF193259),
    onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFF7AA7FF),
    onSecondary = Color(0xFF0B1D3B),
    secondaryContainer = Color(0xFF22242C),
    onSecondaryContainer = Color(0xFFD8E4FF),
    tertiary = Color(0xFFFFC466),
    onTertiary = Color(0xFF2D1B00),
    tertiaryContainer = Color(0xFF3A2A0F),
    onTertiaryContainer = Color(0xFFFFE4B0),
    background = Color(0xFF0F1115),
    onBackground = Color(0xFFF5F7FC),
    surface = Color(0xFF171920),
    onSurface = Color(0xFFF5F7FC),
    surfaceVariant = Color(0xFF272A34),
    onSurfaceVariant = Color(0xFFA3A9B8),
    surfaceTint = Color(0xFF4C8DFF),
    surfaceBright = Color(0xFF30343F),
    surfaceDim = Color(0xFF0F1115),
    surfaceContainerLowest = Color(0xFF0B0D11),
    surfaceContainerLow = Color(0xFF191B22),
    surfaceContainer = Color(0xFF1F2129),
    surfaceContainerHigh = Color(0xFF272A34),
    surfaceContainerHighest = Color(0xFF30343F),
    outline = Color(0xFF414653),
    outlineVariant = Color(0xFF2A2D38),
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
    glow = Color(0x334C8DFF),
    danger = Color(0xFFFF5C6C),
    success = Color(0xFF34D399),
    successContainer = Color(0xFF12362A),
    gridLine = Color(0xFF24262F),
    violet = HackAccent(
        accent = Color(0xFFC4B5FD),
        container = Color(0xFF31264A),
    ),
    amber = HackAccent(
        accent = Color(0xFFFFC466),
        container = Color(0xFF3A2A0F),
    ),
    green = HackAccent(
        accent = Color(0xFF4ADE80),
        container = Color(0xFF143524),
    ),
    pink = HackAccent(
        accent = Color(0xFFF9A8D4),
        container = Color(0xFF3D2033),
    ),
)

val LightHackExtras = HackExtras(
    glow = Color(0x1F2E5BFF),
    danger = Color(0xFFD62A45),
    success = Color(0xFF0F8A5F),
    successContainer = Color(0xFFDDF6E9),
    gridLine = Color(0xFFDCE3F0),
    violet = HackAccent(
        accent = Color(0xFF6D28D9),
        container = Color(0xFFEDE9FE),
    ),
    amber = HackAccent(
        accent = Color(0xFF9A4E00),
        container = Color(0xFFFFF0D6),
    ),
    green = HackAccent(
        accent = Color(0xFF16794C),
        container = Color(0xFFDCFCE7),
    ),
    pink = HackAccent(
        accent = Color(0xFFA21C60),
        container = Color(0xFFFCE7F3),
    ),
)
