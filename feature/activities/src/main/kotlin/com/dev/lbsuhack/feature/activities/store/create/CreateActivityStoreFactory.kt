package com.dev.lbsuhack.feature.activities.store.create

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.toUiText
import kotlinx.coroutines.launch

class CreateActivityStoreFactory(
    private val storeFactory: StoreFactory,
    private val activityRepository: ActivityRepository,
) {

    fun create(): CreateActivityStore =
        object : CreateActivityStore,
            Store<CreateActivityStore.Intent, CreateActivityStore.State, CreateActivityStore.Label> by storeFactory.create(
                name = "CreateActivityStore",
                initialState = CreateActivityStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data class CategoryChanged(val category: ActivityCategory) : Message
        data class TitleChanged(val title: String) : Message
        data class DescriptionChanged(val description: String) : Message
        data class ScopeChanged(val scope: ActivityScope) : Message
        data class VoteThresholdChanged(val threshold: Int) : Message
        data class StartsAtChanged(val startsAt: String) : Message
        data object Submitting : Message
        data class ErrorOccurred(val error: UiText) : Message
        data object ErrorDismissed : Message
        data object Submitted : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<CreateActivityStore.Intent, Nothing, CreateActivityStore.State, Message, CreateActivityStore.Label>() {

        override fun executeIntent(intent: CreateActivityStore.Intent) {
            when (intent) {
                is CreateActivityStore.Intent.ChangeCategory -> dispatch(Message.CategoryChanged(intent.category))
                is CreateActivityStore.Intent.ChangeTitle -> dispatch(Message.TitleChanged(intent.title))
                is CreateActivityStore.Intent.ChangeDescription -> dispatch(Message.DescriptionChanged(intent.description))
                is CreateActivityStore.Intent.ChangeScope -> dispatch(Message.ScopeChanged(intent.scope))
                is CreateActivityStore.Intent.ChangeVoteThreshold -> dispatch(Message.VoteThresholdChanged(intent.threshold))
                is CreateActivityStore.Intent.ChangeStartsAt -> dispatch(Message.StartsAtChanged(intent.startsAt))
                CreateActivityStore.Intent.DismissError -> dispatch(Message.ErrorDismissed)
                CreateActivityStore.Intent.Submit -> submit()
            }
        }

        private fun submit() {
            val currentState = state()
            if (currentState.isSubmitting) return

            if (currentState.title.isBlank()) {
                dispatch(Message.ErrorOccurred(UiText.Resource(R.string.create_activity_error_title_empty)))
                return
            }
            if (currentState.description.isBlank()) {
                dispatch(Message.ErrorOccurred(UiText.Resource(R.string.create_activity_error_desc_empty)))
                return
            }

            scope.launch {
                dispatch(Message.Submitting)

                when (
                    val result = activityRepository.createEvent(
                        name = currentState.title,
                        category = currentState.category,
                        description = currentState.description,
                        scope = currentState.scope,
                        voteThreshold = currentState.voteThreshold,
                        startsAt = currentState.startsAt,
                    )
                ) {
                    is DomainResult.Success -> {
                        dispatch(Message.Submitted)
                        publish(CreateActivityStore.Label.Created(result.value.id))
                    }

                    is DomainResult.Failure -> {
                        dispatch(Message.ErrorOccurred(result.error.toUiText()))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<CreateActivityStore.State, Message> {
        override fun CreateActivityStore.State.reduce(msg: Message): CreateActivityStore.State =
            when (msg) {
                is Message.CategoryChanged -> copy(category = msg.category)
                is Message.TitleChanged -> copy(title = msg.title, error = null)
                is Message.DescriptionChanged -> copy(description = msg.description, error = null)
                is Message.ScopeChanged -> copy(scope = msg.scope)
                is Message.VoteThresholdChanged -> copy(voteThreshold = msg.threshold)
                is Message.StartsAtChanged -> copy(startsAt = msg.startsAt)
                Message.Submitting -> copy(isSubmitting = true, error = null)
                is Message.ErrorOccurred -> copy(isSubmitting = false, error = msg.error)
                Message.ErrorDismissed -> copy(error = null)
                Message.Submitted -> copy(isSubmitting = false)
            }
    }
}
