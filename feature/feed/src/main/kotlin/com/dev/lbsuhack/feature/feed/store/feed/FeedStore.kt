package com.dev.lbsuhack.feature.feed.store.feed

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.feed.domain.EventCategory
import com.dev.lbsuhack.feature.feed.domain.FeedEvent

interface FeedStore : Store<FeedStore.Intent, FeedStore.State, FeedStore.Label> {

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data class SelectCategory(val category: EventCategory?) : Intent
        data class ToggleExpanded(val eventId: String) : Intent
        data class ClickEvent(val eventId: String) : Intent
    }

    data class State(
        val events: List<FeedEvent> = emptyList(),
        val selectedCategory: EventCategory? = null,
        val expandedEventIds: Set<String> = emptySet(),
        val isLoading: Boolean = false,
        val error: UiText? = null,
    ) {
        val visibleEvents: List<FeedEvent>
            get() = if (selectedCategory == null) {
                events
            } else {
                events.filter { it.category == selectedCategory }
            }
    }

    sealed interface Label {
        data class OpenDetails(val eventId: String) : Label
    }
}
