package com.dev.lbsuhack.feature.activities.store.create

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityScope

enum class CreationState {
    EDITING,
    CREATED,
    PHOTO_PENDING,
}

interface CreateActivityStore :
    Store<CreateActivityStore.Intent, CreateActivityStore.State, CreateActivityStore.Label> {

    sealed interface Intent {
        data class ChangeCategory(val category: ActivityCategory) : Intent
        data class ChangeTitle(val title: String) : Intent
        data class ChangeDescription(val description: String) : Intent
        data class ChangeScope(val scope: ActivityScope) : Intent
        data class ChangeVoteThreshold(val threshold: Int) : Intent
        data class ChangePhoto(val photoUri: String?) : Intent
        data object Submit : Intent
        data object DismissError : Intent
    }

    data class State(
        val category: ActivityCategory = ActivityCategory.SPORTS,
        val title: String = "",
        val description: String = "",
        val scope: ActivityScope = ActivityScope.CLASS,
        val voteThreshold: Int = 15,
        val photoUri: String? = null,
        val createdActivityId: String? = null,
        val creationState: CreationState = CreationState.EDITING,
        val isSubmitting: Boolean = false,
        val error: UiText? = null,
        val errorId: Long = 0L,
    ) {
        val canEdit: Boolean
            get() = creationState == CreationState.EDITING

        val canRetryPhoto: Boolean
            get() = creationState == CreationState.PHOTO_PENDING
    }

    sealed interface Label {
        data class Created(val activityId: String) : Label
    }
}
