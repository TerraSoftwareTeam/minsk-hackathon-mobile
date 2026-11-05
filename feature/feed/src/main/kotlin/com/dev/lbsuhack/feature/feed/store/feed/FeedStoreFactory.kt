package com.dev.lbsuhack.feature.feed.store.feed

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.feed.domain.EventCategory
import com.dev.lbsuhack.feature.feed.domain.FeedEvent
import com.dev.lbsuhack.feature.feed.domain.FeedRepository
import com.dev.lbsuhack.feature.feed.toUiText
import kotlinx.coroutines.launch

class FeedStoreFactory(
    private val storeFactory: StoreFactory,
    private val feedRepository: FeedRepository,
) {

    fun create(): FeedStore =
        object : FeedStore,
            Store<FeedStore.Intent, FeedStore.State, FeedStore.Label> by storeFactory.create(
                name = "FeedStore",
                initialState = FeedStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data object Loading : Message
        data class Loaded(val events: List<FeedEvent>) : Message
        data class Failed(val error: UiText) : Message
        data class CategorySelected(val category: EventCategory?) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<FeedStore.Intent, Nothing, FeedStore.State, Message, FeedStore.Label>() {

        override fun executeIntent(intent: FeedStore.Intent) {
            when (intent) {
                FeedStore.Intent.Load, FeedStore.Intent.Refresh -> load()
                is FeedStore.Intent.SelectCategory -> dispatch(Message.CategorySelected(intent.category))
                is FeedStore.Intent.ClickEvent -> publish(FeedStore.Label.OpenDetails(intent.eventId))
            }
        }

        private fun load() {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading)

                when (val result = feedRepository.getEvents()) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<FeedStore.State, Message> {
        override fun FeedStore.State.reduce(msg: Message): FeedStore.State = when (msg) {
            Message.Loading -> copy(isLoading = true, error = null)
            is Message.Loaded -> copy(events = msg.events, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
            is Message.CategorySelected -> copy(selectedCategory = msg.category)
        }
    }
}
