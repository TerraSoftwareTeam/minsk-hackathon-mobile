package com.dev.lbsuhack.feature.createvote.store.createvote

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.createvote.domain.VoteEvent
import com.dev.lbsuhack.feature.createvote.domain.VoteParticipant

interface CreateVoteStore :
    Store<CreateVoteStore.Intent, CreateVoteStore.State, CreateVoteStore.Label> {

    sealed interface Intent {
        data object Load : Intent
        data class SelectEvent(val eventId: String) : Intent
        data class ChangeTitle(val value: String) : Intent
        data class ChangeDescription(val value: String) : Intent
        data class ChangeDeadline(val value: String) : Intent
        data class ToggleParticipant(val participantId: String) : Intent
        data object ToggleAllParticipants : Intent
        data object Submit : Intent
        data object DismissError : Intent
    }

    data class State(
        val events: List<VoteEvent> = emptyList(),
        val selectedEventId: String? = null,
        val title: String = "",
        val description: String = "",
        val deadline: String = "",
        val selectedParticipantIds: Set<String> = emptySet(),
        val isLoading: Boolean = true,
        val isSubmitting: Boolean = false,
        val error: UiText? = null,
    ) {
        val selectedEvent: VoteEvent?
            get() = events.firstOrNull { it.id == selectedEventId }

        val availableParticipants: List<VoteParticipant>
            get() = selectedEvent?.participants.orEmpty()

        val allParticipantsSelected: Boolean
            get() = availableParticipants.isNotEmpty() &&
                selectedParticipantIds.containsAll(availableParticipants.map { it.id })
    }

    sealed interface Label {
        data object Created : Label
    }
}
