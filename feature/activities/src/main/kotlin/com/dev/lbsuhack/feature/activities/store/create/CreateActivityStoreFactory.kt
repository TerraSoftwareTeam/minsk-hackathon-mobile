package com.dev.lbsuhack.feature.activities.store.create

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
import com.dev.lbsuhack.feature.activities.domain.ActivityOrigin
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.store.toUiText
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
                initialState = initialState(),
                bootstrapper = SimpleBootstrapper(Action.LoadChance),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private fun initialState(now: OffsetDateTime = OffsetDateTime.now()): CreateActivityStore.State {
        val role = (observeSessionUseCase.state.value as? SessionState.Authenticated)?.user?.role
        return when (role) {
            UserRole.ORGANIZATOR -> CreateActivityStore.State(
                origin = ActivityOrigin.SCHOOL,
                scope = ActivityScope.SCHOOL,
                startsAt = defaultSchoolStartsAt(now),
            )
            UserRole.TEACHER -> CreateActivityStore.State(origin = ActivityOrigin.TEACHER)
            else -> CreateActivityStore.State()
        }
    }

    private sealed interface Action {
        data object LoadChance : Action
    }

    private sealed interface Message {
        data class CategoryChanged(val category: ActivityCategory) : Message
        data class TitleChanged(val title: String) : Message
        data class DescriptionChanged(val description: String) : Message
        data class ScopeChanged(val scope: ActivityScope) : Message
        data class VoteThresholdChanged(val threshold: Int) : Message
        data class PhotoChanged(val photoUri: String?) : Message
        data class StartsAtChanged(val startsAt: OffsetDateTime, val hasDateError: Boolean) : Message
        data object ChanceLoading : Message
        data class ChanceLoaded(val chance: Double?) : Message
        data class EventCreated(val id: String) : Message
        data object Submitting : Message
        data class ErrorOccurred(val error: UiText) : Message
        data object ErrorDismissed : Message
        data object PhotoFailed : Message
        data object Submitted : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<CreateActivityStore.Intent, Action, CreateActivityStore.State, Message, CreateActivityStore.Label>() {

        private var chanceJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.LoadChance -> loadChance()
            }
        }

        private fun loadChance() {
            chanceJob?.cancel()
            dispatch(Message.ChanceLoading)
            chanceJob = scope.launch {
                delay(CHANCE_DEBOUNCE_MS)
                val current = state()
                val startsAt = current.startsAt?.takeIf { current.requiresDate && !current.hasDateError }
                val result = activityRepository.previewSuccessProbability(current.category, startsAt)
                dispatch(Message.ChanceLoaded((result as? DomainResult.Success)?.value))
            }
        }


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
                is CreateActivityStore.Intent.ChangeCategory -> {
                    if (intent.category != state().category) {
                        dispatch(Message.CategoryChanged(intent.category))
                        loadChance()
                    }
                }
                is CreateActivityStore.Intent.ChangeTitle -> dispatch(Message.TitleChanged(intent.title))
                is CreateActivityStore.Intent.ChangeDescription -> dispatch(Message.DescriptionChanged(intent.description))
                is CreateActivityStore.Intent.ChangeScope -> {
                    if (!state().requiresDate) dispatch(Message.ScopeChanged(intent.scope))
                }
                is CreateActivityStore.Intent.ChangeVoteThreshold -> dispatch(Message.VoteThresholdChanged(intent.threshold))
                is CreateActivityStore.Intent.ChangePhoto -> dispatch(Message.PhotoChanged(intent.photoUri))
                is CreateActivityStore.Intent.ChangeStartsAt -> {
                    dispatch(Message.StartsAtChanged(intent.startsAt, !intent.startsAt.isAfter(OffsetDateTime.now())))
                    loadChance()
                }
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
                        startsAt = currentState.startsAt
                            ?.takeIf { currentState.requiresDate }
                            ?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
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
                is Message.StartsAtChanged -> copy(startsAt = msg.startsAt, hasDateError = msg.hasDateError)
                Message.ChanceLoading -> copy(isChanceLoading = true)
                is Message.ChanceLoaded -> copy(chance = msg.chance, isChanceLoading = false)
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
    requiresDate && startsAt?.isAfter(OffsetDateTime.now()) != true ->
        UiText.Resource(R.string.create_activity_error_date_in_past)
    else -> null
}

private const val MAX_TITLE_LENGTH = 200
private const val MAX_DESCRIPTION_LENGTH = 5000
private const val MIN_VOTE_THRESHOLD = 1
private const val MAX_VOTE_THRESHOLD = 10000

internal fun defaultSchoolStartsAt(now: OffsetDateTime): OffsetDateTime =
    now.plusDays(DEFAULT_START_OFFSET_DAYS).withHour(DEFAULT_START_HOUR).withMinute(0).withSecond(0).withNano(0)

private const val CHANCE_DEBOUNCE_MS = 250L
private const val DEFAULT_START_OFFSET_DAYS = 7L
private const val DEFAULT_START_HOUR = 12
