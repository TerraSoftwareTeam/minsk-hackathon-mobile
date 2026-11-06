package com.dev.lbsuhack.feature.activevote.store.activevote

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activevote.domain.ActiveVote
import com.dev.lbsuhack.feature.activevote.domain.VoteAcceptance

interface ActiveVoteStore :
    Store<ActiveVoteStore.Intent, ActiveVoteStore.State, ActiveVoteStore.Label> {

    sealed interface Intent {
        data class Load(val voteId: String) : Intent
        data class SelectOption(val optionId: String) : Intent
        data object Submit : Intent
        data object DismissError : Intent
    }

    data class State(
        val vote: ActiveVote? = null,
        val selectedOptionId: String? = null,
        val isLoading: Boolean = true,
        val isSubmitting: Boolean = false,
        val hasVoted: Boolean = false,
        val error: UiText? = null,
    )

    sealed interface Label {
        data class VoteAccepted(val acceptance: VoteAcceptance) : Label
    }
}
