package com.dev.lbsuhack.feature.home.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeStoreFactory(
    private val storeFactory: StoreFactory,
) {
    fun create(): HomeStore =
        object : HomeStore, Store<HomeStore.Intent, HomeStore.State, HomeStore.Label> by storeFactory.create(
            name = "HomeStore",
            initialState = HomeStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Message {
        data object Loading : Message
        data class Loaded(val items: List<HomeStore.State.Item>) : Message
    }

    private class ExecutorImpl : CoroutineExecutor<HomeStore.Intent, Nothing, HomeStore.State, Message, HomeStore.Label>() {
        override fun executeIntent(intent: HomeStore.Intent) {
            when (intent) {
                is HomeStore.Intent.ClickItem -> {
                    publish(HomeStore.Label.OpenDetails(intent.id, intent.title))
                }
                is HomeStore.Intent.Refresh -> {
                    scope.launch {
                        dispatch(Message.Loading)
                        delay(600)
                        dispatch(
                            Message.Loaded(
                                listOf(
                                    HomeStore.State.Item("1", "Обзор хакатона (обновлено)", "Правила, расписание и полезные ресурсы"),
                                    HomeStore.State.Item("2", "Навигация в Compose", "Стек Navigation 3 c передачей аргументов"),
                                    HomeStore.State.Item("3", "Архитектура MVI", "MVIKotlin без компонентов Decompose"),
                                    HomeStore.State.Item("4", "Koin DI Multibindings", "Сборка фич и табов в единый host"),
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<HomeStore.State, Message> {
        override fun HomeStore.State.reduce(msg: Message): HomeStore.State =
            when (msg) {
                is Message.Loading -> copy(isLoading = true)
                is Message.Loaded -> copy(items = msg.items, isLoading = false)
            }
    }
}
