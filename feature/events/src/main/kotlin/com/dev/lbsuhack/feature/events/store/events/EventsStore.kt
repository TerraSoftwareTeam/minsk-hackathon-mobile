package com.dev.lbsuhack.feature.events.store.events

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.events.domain.Event

interface EventsStore : Store<EventsStore.Intent, EventsStore.State, EventsStore.Label> {

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data class ClickEvent(val eventId: String) : Intent
    }

    data class State(
        val events: List<Event> = emptyList(),
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label {
        data class OpenDetails(val eventId: String) : Label
    }
}
