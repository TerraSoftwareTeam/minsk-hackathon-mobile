package com.dev.lbsuhack.feature.feed.store.feed

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.feed.domain.FeedPost

interface FeedStore : Store<FeedStore.Intent, FeedStore.State, FeedStore.Label> {

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data class ClickPost(val postId: String) : Intent
    }

    data class State(
        val posts: List<FeedPost> = emptyList(),
        val isLoading: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label {
        data class OpenDetails(val postId: String) : Label
    }
}
