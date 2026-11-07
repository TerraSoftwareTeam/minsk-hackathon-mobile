package com.dev.lbsuhack.core.designsystem.mode

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemeModeTest {

    @Test
    fun `LIGHT uses the light palette`() {
        assertFalse(AppThemeMode.LIGHT.resolveDark())
    }

    @Test
    fun `DARK uses the dark palette`() {
        assertTrue(AppThemeMode.DARK.resolveDark())
    }

    @Test
    fun `default mode is dark because of the brand direction`() {
        assertTrue(DefaultThemeMode == AppThemeMode.DARK)
    }
}
