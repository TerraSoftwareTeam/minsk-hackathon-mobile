package com.dev.lbsuhack.feature.events.store.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.events.domain.Event
import com.dev.lbsuhack.feature.events.domain.EventRepository
import com.dev.lbsuhack.feature.events.toUiText
import kotlinx.coroutines.launch

class EventDetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val eventRepository: EventRepository,
) {

    fun create(): EventDetailsStore =
        object : EventDetailsStore,
            Store<EventDetailsStore.Intent, EventDetailsStore.State, Nothing> by storeFactory.create(
                name = "EventDetailsStore",
                initialState = EventDetailsStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class Loading(val eventId: String) : Message
        data class Loaded(val event: Event) : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<EventDetailsStore.Intent, Nothing, EventDetailsStore.State, Message, Nothing>() {

        override fun executeIntent(intent: EventDetailsStore.Intent) {
            when (intent) {
                is EventDetailsStore.Intent.Load -> load(intent.eventId)
                EventDetailsStore.Intent.Retry -> {
                    val eventId = state().eventId
                    if (eventId.isNotBlank()) load(eventId)
                }
            }
        }

        private fun load(eventId: String) {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading(eventId))

                when (val result = eventRepository.getEvent(eventId)) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<EventDetailsStore.State, Message> {
        override fun EventDetailsStore.State.reduce(msg: Message): EventDetailsStore.State = when (msg) {
            is Message.Loading -> copy(eventId = msg.eventId, isLoading = true, error = null)
            is Message.Loaded -> copy(event = msg.event, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
        }
    }
}
