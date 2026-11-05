package com.dev.lbsuhack.feature.events.store.details

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.events.domain.Event

interface EventDetailsStore : Store<EventDetailsStore.Intent, EventDetailsStore.State, Nothing> {

    sealed interface Intent {
        data class Load(val eventId: String) : Intent
        data object Retry : Intent
    }

    data class State(
        val eventId: String = "",
        val event: Event? = null,
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )
}
