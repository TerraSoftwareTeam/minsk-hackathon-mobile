package com.dev.lbsuhack.feature.home.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.localization.UiText
import com.dev.lbsuhack.feature.home.R
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
                    publish(HomeStore.Label.OpenDetails(intent.id))
                }
                is HomeStore.Intent.Refresh -> {
                    scope.launch {
                        dispatch(Message.Loading)
                        delay(600)
                        dispatch(
                            Message.Loaded(
                                listOf(
                                    HomeStore.State.Item(
                                        id = "1",
                                        title = UiText.Resource(R.string.home_item_1_title_refreshed),
                                        description = UiText.Resource(R.string.home_item_1_description_refreshed),
                                    ),
                                    HomeStore.State.Item(
                                        id = "2",
                                        title = UiText.Resource(R.string.home_item_2_title),
                                        description = UiText.Resource(R.string.home_item_2_description_refreshed),
                                    ),
                                    HomeStore.State.Item(
                                        id = "3",
                                        title = UiText.Resource(R.string.home_item_3_title),
                                        description = UiText.Resource(R.string.home_item_3_description_refreshed),
                                    ),
                                    HomeStore.State.Item(
                                        id = "4",
                                        title = UiText.Resource(R.string.home_item_4_title),
                                        description = UiText.Resource(R.string.home_item_4_description),
                                    ),
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
