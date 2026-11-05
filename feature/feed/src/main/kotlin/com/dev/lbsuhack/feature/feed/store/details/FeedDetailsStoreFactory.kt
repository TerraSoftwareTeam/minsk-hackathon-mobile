package com.dev.lbsuhack.feature.feed.store.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.feed.domain.FeedEvent
import com.dev.lbsuhack.feature.feed.domain.FeedRepository
import com.dev.lbsuhack.feature.feed.toUiText
import kotlinx.coroutines.launch

class FeedDetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val feedRepository: FeedRepository,
) {

    fun create(): FeedDetailsStore =
        object : FeedDetailsStore,
            Store<FeedDetailsStore.Intent, FeedDetailsStore.State, Nothing> by storeFactory.create(
                name = "FeedDetailsStore",
                initialState = FeedDetailsStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class Loading(val eventId: String) : Message
        data class Loaded(val event: FeedEvent) : Message
        data class Failed(val error: UiText) : Message
        data object SubscriptionChanging : Message
        data class SubscriptionChanged(val event: FeedEvent) : Message
        data class SubscriptionFailed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<FeedDetailsStore.Intent, Nothing, FeedDetailsStore.State, Message, Nothing>() {

        override fun executeIntent(intent: FeedDetailsStore.Intent) {
            when (intent) {
                is FeedDetailsStore.Intent.Load -> load(intent.eventId)
                FeedDetailsStore.Intent.Retry -> {
                    val eventId = state().eventId
                    if (eventId.isNotBlank()) load(eventId)
                }
                FeedDetailsStore.Intent.ToggleSubscription -> toggleSubscription()
            }
        }

        private fun load(eventId: String) {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading(eventId))

                when (val result = feedRepository.getEvent(eventId)) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }

        private fun toggleSubscription() {
            val event = state().event ?: return
            if (state().isSubscribing) return

            scope.launch {
                dispatch(Message.SubscriptionChanging)

                val target = !event.isSubscribed
                when (val result = feedRepository.setSubscribed(event.id, target)) {
                    is DomainResult.Success -> dispatch(Message.SubscriptionChanged(result.value))
                    is DomainResult.Failure -> dispatch(Message.SubscriptionFailed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<FeedDetailsStore.State, Message> {
        override fun FeedDetailsStore.State.reduce(msg: Message): FeedDetailsStore.State = when (msg) {
            is Message.Loading -> copy(eventId = msg.eventId, isLoading = true, error = null)
            is Message.Loaded -> copy(event = msg.event, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
            Message.SubscriptionChanging -> copy(isSubscribing = true, error = null)
            is Message.SubscriptionChanged -> copy(event = msg.event, isSubscribing = false)
            is Message.SubscriptionFailed -> copy(isSubscribing = false, error = msg.error)
        }
    }
}
