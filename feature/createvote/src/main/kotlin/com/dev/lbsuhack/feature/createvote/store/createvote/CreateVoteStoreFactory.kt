package com.dev.lbsuhack.feature.createvote.store.createvote

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.createvote.R
import com.dev.lbsuhack.feature.createvote.domain.VoteDraft
import com.dev.lbsuhack.feature.createvote.domain.VoteEvent
import com.dev.lbsuhack.feature.createvote.domain.VoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateVoteStoreFactory(
    private val storeFactory: StoreFactory,
    private val voteRepository: VoteRepository,
) {

    fun create(): CreateVoteStore =
        object : CreateVoteStore,
            Store<CreateVoteStore.Intent, CreateVoteStore.State, CreateVoteStore.Label> by storeFactory.create(
                name = "CreateVoteStore",
                initialState = CreateVoteStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data object Loading : Message
        data class EventsLoaded(val events: List<VoteEvent>) : Message
        data class EventSelected(val event: VoteEvent) : Message
        data class TitleChanged(val value: String) : Message
        data class DescriptionChanged(val value: String) : Message
        data class DeadlineChanged(val value: String) : Message
        data class ParticipantToggled(val participantId: String) : Message
        data class SelectedParticipantsChanged(val participantIds: Set<String>) : Message
        data object Submitting : Message
        data class ErrorOccurred(val error: UiText) : Message
        data object ErrorDismissed : Message
        data object Submitted : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<CreateVoteStore.Intent, Nothing, CreateVoteStore.State, Message, CreateVoteStore.Label>() {

        private var observing = false

        override fun executeIntent(intent: CreateVoteStore.Intent) {
            when (intent) {
                CreateVoteStore.Intent.Load -> load()
                is CreateVoteStore.Intent.SelectEvent -> selectEvent(intent.eventId)
                is CreateVoteStore.Intent.ChangeTitle -> dispatch(Message.TitleChanged(intent.value))
                is CreateVoteStore.Intent.ChangeDescription -> dispatch(Message.DescriptionChanged(intent.value))
                is CreateVoteStore.Intent.ChangeDeadline -> dispatch(Message.DeadlineChanged(intent.value))
                is CreateVoteStore.Intent.ToggleParticipant -> toggleParticipant(intent.participantId)
                CreateVoteStore.Intent.ToggleAllParticipants -> toggleAllParticipants()
                CreateVoteStore.Intent.Submit -> submit()
                CreateVoteStore.Intent.DismissError -> dispatch(Message.ErrorDismissed)
            }
        }

        private fun load() {
            if (observing) return
            observing = true

            scope.launch {
                dispatch(Message.Loading)
                voteRepository.observeEvents().collectLatest { events ->
                    dispatch(Message.EventsLoaded(events))
                }
            }
        }

        private fun selectEvent(eventId: String) {
            val event = state().events.firstOrNull { it.id == eventId } ?: return
            dispatch(Message.EventSelected(event))
        }

        private fun toggleParticipant(participantId: String) {
            if (state().availableParticipants.none { it.id == participantId }) return
            dispatch(Message.ParticipantToggled(participantId))
        }

        private fun toggleAllParticipants() {
            val currentState = state()
            val availableIds = currentState.availableParticipants.map { it.id }.toSet()
            if (availableIds.isEmpty()) return

            val selectedIds = if (currentState.selectedParticipantIds.containsAll(availableIds)) {
                emptySet()
            } else {
                availableIds
            }
            dispatch(Message.SelectedParticipantsChanged(selectedIds))
        }

        private fun submit() {
            val currentState = state()
            if (currentState.isSubmitting) return

            val event = currentState.selectedEvent
            if (event == null) {
                dispatch(Message.ErrorOccurred(UiText.Resource(R.string.create_vote_error_event)))
                return
            }
            if (currentState.title.isBlank()) {
                dispatch(Message.ErrorOccurred(UiText.Resource(R.string.create_vote_error_title)))
                return
            }
            if (currentState.deadline.isBlank()) {
                dispatch(Message.ErrorOccurred(UiText.Resource(R.string.create_vote_error_deadline)))
                return
            }
            if (currentState.selectedParticipantIds.isEmpty()) {
                dispatch(Message.ErrorOccurred(UiText.Resource(R.string.create_vote_error_participants)))
                return
            }

            scope.launch {
                dispatch(Message.Submitting)
                try {
                    voteRepository.createVote(
                        VoteDraft(
                            eventId = event.id,
                            title = currentState.title,
                            description = currentState.description,
                            deadline = currentState.deadline,
                            participantIds = currentState.selectedParticipantIds,
                        ),
                    )
                    dispatch(Message.Submitted)
                    publish(CreateVoteStore.Label.Created)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    dispatch(Message.ErrorOccurred(UiText.Resource(R.string.create_vote_error_failed)))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<CreateVoteStore.State, Message> {
        override fun CreateVoteStore.State.reduce(msg: Message): CreateVoteStore.State =
            when (msg) {
                Message.Loading -> copy(isLoading = true)
                is Message.EventsLoaded -> copy(events = msg.events, isLoading = false)
                is Message.EventSelected -> {
                    val availableIds = msg.event.participants.map { it.id }.toSet()
                    copy(
                        selectedEventId = msg.event.id,
                        selectedParticipantIds = selectedParticipantIds.intersect(availableIds),
                        title = if (title.isBlank()) msg.event.title else title,
                        error = null,
                    )
                }

                is Message.TitleChanged -> copy(title = msg.value, error = null)
                is Message.DescriptionChanged -> copy(description = msg.value, error = null)
                is Message.DeadlineChanged -> copy(deadline = msg.value, error = null)
                is Message.ParticipantToggled -> copy(
                    selectedParticipantIds = if (selectedParticipantIds.contains(msg.participantId)) {
                        selectedParticipantIds - msg.participantId
                    } else {
                        selectedParticipantIds + msg.participantId
                    },
                    error = null,
                )

                is Message.SelectedParticipantsChanged -> copy(
                    selectedParticipantIds = msg.participantIds,
                    error = null,
                )

                Message.Submitting -> copy(isSubmitting = true, error = null)
                is Message.ErrorOccurred -> copy(isSubmitting = false, error = msg.error)
                Message.ErrorDismissed -> copy(error = null)
                Message.Submitted -> copy(isSubmitting = false)
            }
    }
}
