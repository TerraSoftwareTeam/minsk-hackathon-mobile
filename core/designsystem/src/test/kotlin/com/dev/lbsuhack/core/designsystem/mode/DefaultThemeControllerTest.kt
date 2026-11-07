package com.dev.lbsuhack.core.designsystem.mode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultThemeControllerTest {

    private class FakeThemePreferences(initial: AppThemeMode) : ThemePreferences {
        private val state = MutableStateFlow(initial)
        override val mode: StateFlow<AppThemeMode> = state.asStateFlow()

        var persisted: AppThemeMode? = null
            private set

        override fun setMode(mode: AppThemeMode) {
            persisted = mode
            state.value = mode
        }
    }

    @Test
    fun `current reflects the stored mode`() {
        val controller = DefaultThemeController(FakeThemePreferences(AppThemeMode.LIGHT))

        assertEquals(AppThemeMode.LIGHT, controller.current.value)
    }

    @Test
    fun `every mode is offered to the ui`() {
        val controller = DefaultThemeController(FakeThemePreferences(DefaultThemeMode))

        assertEquals(AppThemeMode.entries, controller.availableModes)
        assertEquals(listOf(AppThemeMode.DARK, AppThemeMode.LIGHT), controller.availableModes)
    }

    @Test
    fun `setMode updates current and forwards to storage`() {
        val preferences = FakeThemePreferences(DefaultThemeMode)
        val controller = DefaultThemeController(preferences)

        controller.setMode(AppThemeMode.LIGHT)

        assertEquals(AppThemeMode.LIGHT, controller.current.value)
        assertEquals(AppThemeMode.LIGHT, preferences.persisted)
    }
}
