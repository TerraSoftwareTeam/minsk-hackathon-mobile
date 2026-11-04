package com.dev.lbsuhack.core.designsystem.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemeModeTest {

    @Test
    fun `SYSTEM follows the system dark setting`() {
        assertTrue(AppThemeMode.SYSTEM.resolveDark(systemInDark = true))
        assertFalse(AppThemeMode.SYSTEM.resolveDark(systemInDark = false))
    }

    @Test
    fun `LIGHT ignores the system dark setting`() {
        assertFalse(AppThemeMode.LIGHT.resolveDark(systemInDark = true))
        assertFalse(AppThemeMode.LIGHT.resolveDark(systemInDark = false))
    }

    @Test
    fun `DARK ignores the system dark setting`() {
        assertTrue(AppThemeMode.DARK.resolveDark(systemInDark = true))
        assertTrue(AppThemeMode.DARK.resolveDark(systemInDark = false))
    }

    @Test
    fun `default mode is dark because of the brand direction`() {
        assertTrue(DefaultThemeMode == AppThemeMode.DARK)
    }
}
