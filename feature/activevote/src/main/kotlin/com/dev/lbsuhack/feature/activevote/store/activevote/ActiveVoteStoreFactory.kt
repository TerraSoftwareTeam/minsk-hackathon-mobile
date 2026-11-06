package com.dev.lbsuhack.feature.activevote.store.activevote

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activevote.R
import com.dev.lbsuhack.feature.activevote.domain.ActiveVote
import com.dev.lbsuhack.feature.activevote.domain.ActiveVoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ActiveVoteStoreFactory(
    private val storeFactory: StoreFactory,
    private val activeVoteRepository: ActiveVoteRepository,
) {

    fun create(): ActiveVoteStore =
        object : ActiveVoteStore,
            Store<ActiveVoteStore.Intent, ActiveVoteStore.State, ActiveVoteStore.Label> by storeFactory.create(
                name = "ActiveVoteStore",
                initialState = ActiveVoteStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class VoteLoaded(val vote: ActiveVote?) : Message
        data class VoteStatusUpdated(val hasVoted: Boolean) : Message
        data class OptionSelected(val optionId: String) : Message
        data object Submitting : Message
        data class ErrorOccurred(val error: UiText) : Message
        data object ErrorDismissed : Message
        data object Submitted : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ActiveVoteStore.Intent, Nothing, ActiveVoteStore.State, Message, ActiveVoteStore.Label>() {

        private var voteId: String? = null
        private var observing = false

        override fun executeIntent(intent: ActiveVoteStore.Intent) {
            when (intent) {
                is ActiveVoteStore.Intent.Load -> {
                    voteId = intent.voteId
                    startObserving()
                }

                is ActiveVoteStore.Intent.SelectOption -> selectOption(intent.optionId)
                ActiveVoteStore.Intent.Submit -> submit()
                ActiveVoteStore.Intent.DismissError -> dispatch(Message.ErrorDismissed)
            }
        }

        private fun startObserving() {
            if (observing) return
            observing = true

            scope.launch {
                activeVoteRepository.observeVotes().collectLatest { votes ->
                    dispatch(Message.VoteLoaded(votes.firstOrNull { it.id == voteId }))
                }
            }

            scope.launch {
                activeVoteRepository.observeResults().collectLatest { results ->
                    dispatch(Message.VoteStatusUpdated(results.any { it.voteId == voteId }))
                }
            }
        }

        private fun selectOption(optionId: String) {
            val currentState = state()
            val vote = currentState.vote ?: return
            if (currentState.hasVoted) return
            if (vote.options.none { it.id == optionId }) return

            dispatch(Message.OptionSelected(optionId))
        }

        private fun submit() {
            val currentState = state()
            val vote = currentState.vote ?: return
            if (currentState.isSubmitting || currentState.hasVoted) return

            val optionId = currentState.selectedOptionId
            if (optionId == null) {
                dispatch(Message.ErrorOccurred(UiText.Resource(R.string.active_vote_error_option)))
                return
            }

            scope.launch {
                dispatch(Message.Submitting)
                try {
                    val acceptance = activeVoteRepository.submitVote(
                        voteId = vote.id,
                        optionId = optionId,
                    )
                    dispatch(Message.Submitted)
                    publish(ActiveVoteStore.Label.VoteAccepted(acceptance))
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    dispatch(Message.ErrorOccurred(UiText.Resource(R.string.active_vote_error_failed)))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ActiveVoteStore.State, Message> {
        override fun ActiveVoteStore.State.reduce(msg: Message): ActiveVoteStore.State =
            when (msg) {
                is Message.VoteLoaded -> copy(vote = msg.vote, isLoading = false)
                is Message.VoteStatusUpdated -> copy(hasVoted = msg.hasVoted)
                is Message.OptionSelected -> copy(selectedOptionId = msg.optionId, error = null)
                Message.Submitting -> copy(isSubmitting = true, error = null)
                is Message.ErrorOccurred -> copy(isSubmitting = false, error = msg.error)
                Message.ErrorDismissed -> copy(error = null)
                Message.Submitted -> copy(isSubmitting = false, hasVoted = true)
            }
    }
}
