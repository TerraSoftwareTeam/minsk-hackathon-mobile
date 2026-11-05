package com.dev.lbsuhack.feature.events.store.events

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

class EventsStoreFactory(
    private val storeFactory: StoreFactory,
    private val eventRepository: EventRepository,
) {

    fun create(): EventsStore =
        object : EventsStore,
            Store<EventsStore.Intent, EventsStore.State, EventsStore.Label> by storeFactory.create(
                name = "EventsStore",
                initialState = EventsStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data object Loading : Message
        data class Loaded(val events: List<Event>) : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<EventsStore.Intent, Nothing, EventsStore.State, Message, EventsStore.Label>() {

        override fun executeIntent(intent: EventsStore.Intent) {
            when (intent) {
                EventsStore.Intent.Load, EventsStore.Intent.Refresh -> load()
                is EventsStore.Intent.ClickEvent ->
                    publish(EventsStore.Label.OpenDetails(intent.eventId))
            }
        }

        private fun load() {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading)

                when (val result = eventRepository.getEvents()) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<EventsStore.State, Message> {
        override fun EventsStore.State.reduce(msg: Message): EventsStore.State = when (msg) {
            Message.Loading -> copy(isLoading = true, error = null)
            is Message.Loaded -> copy(events = msg.events, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
        }
    }
}
