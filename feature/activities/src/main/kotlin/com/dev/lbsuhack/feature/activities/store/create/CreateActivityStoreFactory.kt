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
import com.dev.lbsuhack.feature.activities.store.ActivityOperation
import com.dev.lbsuhack.feature.activities.store.toUiText
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
        data class PhotoChanged(val photoUri: String?) : Message
        data class EventCreated(val id: String) : Message
        data object Submitting : Message
        data class ErrorOccurred(val error: UiText) : Message
        data object ErrorDismissed : Message
        data object PhotoFailed : Message
        data object Submitted : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<CreateActivityStore.Intent, Nothing, CreateActivityStore.State, Message, CreateActivityStore.Label>() {

        override fun executeIntent(intent: CreateActivityStore.Intent) {
            when (state().creationState) {
                CreationState.EDITING -> handle(intent)
                CreationState.PHOTO_PENDING -> when (intent) {
                    is CreateActivityStore.Intent.ChangePhoto,
                    CreateActivityStore.Intent.Submit,
                    CreateActivityStore.Intent.DismissError,
                    -> handle(intent)

                    else -> Unit
                }

                CreationState.CREATED -> when (intent) {
                    CreateActivityStore.Intent.DismissError -> handle(intent)
                    else -> Unit
                }
            }
        }

        private fun handle(intent: CreateActivityStore.Intent) {
            when (intent) {
                is CreateActivityStore.Intent.ChangeCategory -> dispatch(Message.CategoryChanged(intent.category))
                is CreateActivityStore.Intent.ChangeTitle -> dispatch(Message.TitleChanged(intent.title))
                is CreateActivityStore.Intent.ChangeDescription -> dispatch(Message.DescriptionChanged(intent.description))
                is CreateActivityStore.Intent.ChangeScope -> dispatch(Message.ScopeChanged(intent.scope))
                is CreateActivityStore.Intent.ChangeVoteThreshold -> dispatch(Message.VoteThresholdChanged(intent.threshold))
                is CreateActivityStore.Intent.ChangePhoto -> dispatch(Message.PhotoChanged(intent.photoUri))
                CreateActivityStore.Intent.DismissError -> dispatch(Message.ErrorDismissed)
                CreateActivityStore.Intent.Submit -> submit()
            }
        }

        private fun submit() {
            val currentState = state()
            if (currentState.isSubmitting) return

            if (currentState.createdActivityId == null) {
                val validationError = currentState.validationError()
                if (validationError != null) {
                    dispatch(Message.ErrorOccurred(validationError))
                    return
                }
            }

            dispatch(Message.Submitting)
            scope.launch {
                val existingId = currentState.createdActivityId
                val id = if (existingId != null) {
                    existingId
                } else {
                    when (val result = activityRepository.createEvent(
                        name = currentState.title,
                        category = currentState.category,
                        description = currentState.description,
                        scope = currentState.scope,
                        voteThreshold = currentState.voteThreshold,
                    )) {
                        is DomainResult.Success -> result.value.id.also { dispatch(Message.EventCreated(it)) }
                        is DomainResult.Failure -> {
                            dispatch(Message.ErrorOccurred(result.error.toUiText(ActivityOperation.CREATE)))
                            return@launch
                        }
                    }
                }
                val photoUri = currentState.photoUri
                if (photoUri != null) {
                    when (activityRepository.uploadPhoto(id, photoUri)) {
                        is DomainResult.Success -> Unit
                        is DomainResult.Failure -> {
                            dispatch(Message.PhotoFailed)
                            dispatch(
                                Message.ErrorOccurred(
                                    UiText.Resource(R.string.create_activity_error_photo_upload),
                                ),
                            )
                            return@launch
                        }
                    }
                }
                dispatch(Message.Submitted)
                publish(CreateActivityStore.Label.Created(id))
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
                is Message.PhotoChanged -> copy(photoUri = msg.photoUri)
                is Message.EventCreated -> copy(createdActivityId = msg.id)
                Message.Submitting -> copy(isSubmitting = true, error = null)
                is Message.ErrorOccurred -> copy(isSubmitting = false, error = msg.error, errorId = errorId + 1)
                Message.ErrorDismissed -> copy(error = null)
                Message.PhotoFailed -> copy(isSubmitting = false, creationState = CreationState.PHOTO_PENDING)
                Message.Submitted -> copy(isSubmitting = false, creationState = CreationState.CREATED)
            }
    }
}

private fun CreateActivityStore.State.validationError(): UiText? = when {
    title.isBlank() -> UiText.Resource(R.string.create_activity_error_title_empty)
    description.isBlank() -> UiText.Resource(R.string.create_activity_error_desc_empty)
    title.trim().length > MAX_TITLE_LENGTH -> UiText.Resource(R.string.create_activity_error_limits)
    description.trim().length > MAX_DESCRIPTION_LENGTH -> UiText.Resource(R.string.create_activity_error_limits)
    voteThreshold !in MIN_VOTE_THRESHOLD..MAX_VOTE_THRESHOLD -> UiText.Resource(R.string.create_activity_error_limits)
    else -> null
}

private const val MAX_TITLE_LENGTH = 200
private const val MAX_DESCRIPTION_LENGTH = 5000
private const val MIN_VOTE_THRESHOLD = 1
private const val MAX_VOTE_THRESHOLD = 10000
