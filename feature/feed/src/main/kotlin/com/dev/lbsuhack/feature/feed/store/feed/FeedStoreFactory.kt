package com.dev.lbsuhack.feature.feed.store.feed

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.feed.domain.FeedPost
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
        data class Loaded(val posts: List<FeedPost>) : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<FeedStore.Intent, Nothing, FeedStore.State, Message, FeedStore.Label>() {

        override fun executeIntent(intent: FeedStore.Intent) {
            when (intent) {
                FeedStore.Intent.Load, FeedStore.Intent.Refresh -> load()
                is FeedStore.Intent.ClickPost -> publish(FeedStore.Label.OpenDetails(intent.postId))
            }
        }

        private fun load() {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading)

                when (val result = feedRepository.getFeed()) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<FeedStore.State, Message> {
        override fun FeedStore.State.reduce(msg: Message): FeedStore.State = when (msg) {
            Message.Loading -> copy(isLoading = true, error = null)
            is Message.Loaded -> copy(posts = msg.posts, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
        }
    }
}
