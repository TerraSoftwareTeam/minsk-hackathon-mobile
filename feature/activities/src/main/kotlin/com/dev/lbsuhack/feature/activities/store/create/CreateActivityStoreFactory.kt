package com.dev.lbsuhack.feature.activities.store.create

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import kotlinx.coroutines.launch

class CreateActivityStoreFactory(
    private val storeFactory: StoreFactory,
    private val activityRepository: ActivityRepository,
    private val observeSessionUseCase: ObserveSessionUseCase,
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
        data class TargetClassChanged(val targetClass: String) : Message
        data class MinParticipantsChanged(val min: Int) : Message
        data class MaxParticipantsChanged(val max: Int) : Message
        data class PreferencesChanged(val preferences: String) : Message
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
                is CreateActivityStore.Intent.ChangeTargetClass -> dispatch(Message.TargetClassChanged(intent.targetClass))
                is CreateActivityStore.Intent.ChangeMinParticipants -> dispatch(Message.MinParticipantsChanged(intent.min))
                is CreateActivityStore.Intent.ChangeMaxParticipants -> dispatch(Message.MaxParticipantsChanged(intent.max))
                is CreateActivityStore.Intent.ChangePreferences -> dispatch(Message.PreferencesChanged(intent.preferences))
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
            if (currentState.minParticipants > currentState.maxParticipants) {
                dispatch(Message.ErrorOccurred(UiText.Resource(R.string.create_activity_error_limits_invalid)))
                return
            }

            scope.launch {
                dispatch(Message.Submitting)
                val sessionUser = (observeSessionUseCase.state.value as? SessionState.Authenticated)?.user
                val authorId = sessionUser?.id ?: "101"
                val authorName = sessionUser?.name ?: "Никита Ковалев"
                val authorClass = sessionUser?.pupilClass ?: "10-А"

                val created = activityRepository.createProposal(
                    title = currentState.title,
                    category = currentState.category,
                    description = currentState.description,
                    targetClass = currentState.targetClass,
                    minParticipants = currentState.minParticipants,
                    maxParticipants = currentState.maxParticipants,
                    preferenceNotes = currentState.preferenceNotes,
                    authorId = authorId,
                    authorName = authorName,
                    authorClass = authorClass,
                )

                dispatch(Message.Submitted)
                publish(CreateActivityStore.Label.Created(created.id))
            }
        }
    }

    private object ReducerImpl : Reducer<CreateActivityStore.State, Message> {
        override fun CreateActivityStore.State.reduce(msg: Message): CreateActivityStore.State =
            when (msg) {
                is Message.CategoryChanged -> copy(category = msg.category)
                is Message.TitleChanged -> copy(title = msg.title, error = null)
                is Message.DescriptionChanged -> copy(description = msg.description, error = null)
                is Message.TargetClassChanged -> copy(targetClass = msg.targetClass)
                is Message.MinParticipantsChanged -> copy(minParticipants = msg.min, error = null)
                is Message.MaxParticipantsChanged -> copy(maxParticipants = msg.max, error = null)
                is Message.PreferencesChanged -> copy(preferenceNotes = msg.preferences)
                Message.Submitting -> copy(isSubmitting = true, error = null)
                is Message.ErrorOccurred -> copy(isSubmitting = false, error = msg.error)
                Message.ErrorDismissed -> copy(error = null)
                Message.Submitted -> copy(isSubmitting = false)
            }
    }
}
