package com.dev.lbsuhack.core.designsystem.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.designsystem.mode.resolveDark

internal val LocalHackExtras = staticCompositionLocalOf<HackExtras> {
    error("HackTheme is not provided. Wrap the content with HackTheme { }.")
}


val MaterialTheme.hack: HackExtras
    @Composable
    @ReadOnlyComposable
    get() = LocalHackExtras.current


@Composable
fun HackTheme(
    mode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDark = mode.resolveDark(isSystemInDarkTheme())

    SyncSystemBarIcons(isDark = isDark)

    CompositionLocalProvider(
        LocalHackExtras provides if (isDark) DarkHackExtras else LightHackExtras,
    ) {
        MaterialTheme(
            colorScheme = if (isDark) DarkHackColorScheme else LightHackColorScheme,
            typography = HackTypography,
            shapes = HackShapes,
            content = content,
        )
    }
}

@Composable
private fun SyncSystemBarIcons(isDark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = view.context.findActivity() ?: return
    val window = activity.window

    LaunchedEffect(window, view, isDark) {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
