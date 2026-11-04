package com.dev.lbsuhack.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.mode.ThemeController
import com.dev.lbsuhack.core.designsystem.theme.HackTheme
import org.koin.compose.koinInject

/**
 * Connects the persisted theme to the design system.
 *
 * Kept in `app` on purpose: features must not know about Koin injection of
 * app-level infrastructure.
 */
@Composable
fun AppTheme(
    controller: ThemeController = koinInject(),
    content: @Composable () -> Unit,
) {
    val mode by controller.current.collectAsStateWithLifecycle()

    HackTheme(mode = mode, content = content)
}
