package com.dev.lbsuhack.feature.feed.store.details

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.feed.domain.FeedEvent

interface FeedDetailsStore : Store<FeedDetailsStore.Intent, FeedDetailsStore.State, Nothing> {

    sealed interface Intent {
        data class Load(val eventId: String) : Intent
        data object Retry : Intent
        data object ToggleSubscription : Intent
    }

    data class State(
        val eventId: String = "",
        val event: FeedEvent? = null,
        val isSubscribing: Boolean = false,
        val isLoading: Boolean = false,
        val error: UiText? = null,
    ) {
        val isSubscribed: Boolean
            get() = event?.isSubscribed == true
    }
}
