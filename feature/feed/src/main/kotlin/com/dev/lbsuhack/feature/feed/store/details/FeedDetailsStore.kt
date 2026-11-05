package com.dev.lbsuhack.feature.feed.store.details

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.feed.domain.FeedPost

interface FeedDetailsStore : Store<FeedDetailsStore.Intent, FeedDetailsStore.State, Nothing> {

    sealed interface Intent {
        data class Load(val postId: String) : Intent
        data object Retry : Intent
    }

    data class State(
        val postId: String = "",
        val post: FeedPost? = null,
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )
}
