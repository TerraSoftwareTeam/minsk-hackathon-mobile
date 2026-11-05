package com.dev.lbsuhack.feature.profile.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.localization.language.AppLanguage
import com.dev.lbsuhack.feature.profile.store.settings.SettingsStore
import com.dev.lbsuhack.feature.profile.store.settings.SettingsStoreFactory
import com.dev.lbsuhack.feature.profile.util.FakeLanguageController
import com.dev.lbsuhack.feature.profile.util.FakeThemeController
import com.dev.lbsuhack.feature.profile.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val themeController = FakeThemeController(initial = AppThemeMode.DARK)
    private val languageController = FakeLanguageController(initial = AppLanguage.SYSTEM)

    private fun createStore(): SettingsStore = SettingsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        themeController = themeController,
        languageController = languageController,
    ).create()

    @Test
    fun `initial state mirrors the controllers`() {
        val store = createStore()

        assertEquals(AppThemeMode.DARK, store.state.theme)
        assertEquals(AppLanguage.SYSTEM, store.state.language)
    }

    @Test
    fun `set theme updates the controller and the state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(SettingsStore.Intent.SetTheme(AppThemeMode.LIGHT))
            runCurrent()

            assertEquals(AppThemeMode.LIGHT, store.state.theme)
            assertEquals(AppThemeMode.LIGHT, themeController.current.value)
        }

    @Test
    fun `set language updates the controller and the state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(SettingsStore.Intent.SetLanguage(AppLanguage.RUSSIAN))
            runCurrent()

            assertEquals(AppLanguage.RUSSIAN, store.state.language)
            assertEquals(AppLanguage.RUSSIAN, languageController.current.value)
        }
}
