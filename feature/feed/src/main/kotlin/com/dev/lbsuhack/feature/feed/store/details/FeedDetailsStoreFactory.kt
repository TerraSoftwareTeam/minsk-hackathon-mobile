package com.dev.lbsuhack.feature.feed.store.details

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
        data class Loading(val postId: String) : Message
        data class Loaded(val post: FeedPost) : Message
        data class Failed(val error: UiText) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<FeedDetailsStore.Intent, Nothing, FeedDetailsStore.State, Message, Nothing>() {

        override fun executeIntent(intent: FeedDetailsStore.Intent) {
            when (intent) {
                is FeedDetailsStore.Intent.Load -> load(intent.postId)
                FeedDetailsStore.Intent.Retry -> {
                    val postId = state().postId
                    if (postId.isNotBlank()) load(postId)
                }
            }
        }

        private fun load(postId: String) {
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.Loading(postId))

                when (val result = feedRepository.getPost(postId)) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<FeedDetailsStore.State, Message> {
        override fun FeedDetailsStore.State.reduce(msg: Message): FeedDetailsStore.State = when (msg) {
            is Message.Loading -> copy(postId = msg.postId, isLoading = true, error = null)
            is Message.Loaded -> copy(post = msg.post, isLoading = false, error = null)
            is Message.Failed -> copy(isLoading = false, error = msg.error)
        }
    }
}
