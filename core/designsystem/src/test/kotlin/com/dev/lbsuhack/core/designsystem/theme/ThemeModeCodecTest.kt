package com.dev.lbsuhack.core.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeCodecTest {

    @Test
    fun `every mode survives an encode decode round trip`() {
        AppThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeModeCodec.decode(ThemeModeCodec.encode(mode)))
        }
    }

    @Test
    fun `missing stored value falls back to the default mode`() {
        assertEquals(DefaultThemeMode, ThemeModeCodec.decode(null))
    }

    @Test
    fun `unknown stored value falls back to the default mode`() {
        assertEquals(DefaultThemeMode, ThemeModeCodec.decode("SEPIA"))
    }

    @Test
    fun `persisted values are stable names`() {
        assertEquals("SYSTEM", ThemeModeCodec.encode(AppThemeMode.SYSTEM))
        assertEquals("LIGHT", ThemeModeCodec.encode(AppThemeMode.LIGHT))
        assertEquals("DARK", ThemeModeCodec.encode(AppThemeMode.DARK))
    }
}
