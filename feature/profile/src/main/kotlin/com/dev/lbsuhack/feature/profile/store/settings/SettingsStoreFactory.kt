package com.dev.lbsuhack.feature.profile.store.settings

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.designsystem.mode.AppThemeMode
import com.dev.lbsuhack.core.designsystem.mode.ThemeController
import com.dev.lbsuhack.core.localization.language.AppLanguage
import com.dev.lbsuhack.core.localization.language.LanguageController
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    private val themeController: ThemeController,
    private val languageController: LanguageController,
) {

    fun create(): SettingsStore =
        object : SettingsStore,
            Store<SettingsStore.Intent, SettingsStore.State, Nothing> by storeFactory.create(
                name = "SettingsStore",
                initialState = SettingsStore.State(
                    theme = themeController.current.value,
                    language = languageController.current.value,
                    availableModes = themeController.availableModes,
                    availableLanguages = languageController.availableLanguages,
                ),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class ThemeChanged(val mode: AppThemeMode) : Message
        data class LanguageChanged(val language: AppLanguage) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<SettingsStore.Intent, Nothing, SettingsStore.State, Message, Nothing>() {

        private var observeJob: Job? = null

        override fun executeIntent(intent: SettingsStore.Intent) {
            when (intent) {
                SettingsStore.Intent.Init -> startObserving()
                is SettingsStore.Intent.SetTheme -> {
                    themeController.setMode(intent.mode)
                    dispatch(Message.ThemeChanged(intent.mode))
                }

                is SettingsStore.Intent.SetLanguage -> {
                    languageController.setLanguage(intent.language)
                    dispatch(Message.LanguageChanged(intent.language))
                }
            }
        }

        private fun startObserving() {
            if (observeJob != null) return
            observeJob = scope.launch {
                launch {
                    themeController.current.collect { mode ->
                        dispatch(Message.ThemeChanged(mode))
                    }
                }
                launch {
                    languageController.current.collect { language ->
                        dispatch(Message.LanguageChanged(language))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<SettingsStore.State, Message> {
        override fun SettingsStore.State.reduce(msg: Message): SettingsStore.State = when (msg) {
            is Message.ThemeChanged -> copy(theme = msg.mode)
            is Message.LanguageChanged -> copy(language = msg.language)
        }
    }
}
