package com.dev.lbsuhack.feature.activities.store.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
import com.dev.lbsuhack.feature.activities.store.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class ActivityDetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val activityRepository: ActivityRepository,
    private val observeSessionUseCase: ObserveSessionUseCase,
) {

    fun create(activityId: String? = null): ActivityDetailsStore =
        object : ActivityDetailsStore,
            Store<ActivityDetailsStore.Intent, ActivityDetailsStore.State, ActivityDetailsStore.Label> by storeFactory.create(
                name = "ActivityDetailsStore",
                initialState = ActivityDetailsStore.State(
                    activity = activityId?.let(activityRepository::cachedEventById),
                    currentUser = (observeSessionUseCase.state.value as? SessionState.Authenticated)?.user,
                ),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data object ClearError : Message
        data object Loading : Message
        data class Loaded(val activity: ActivityProposal) : Message
        data class LoadFailed(val loadState: ActivityLoadState) : Message
        data object ActivityRemoved : Message
        data class ActionFailed(val error: UiText) : Message
        data class UserUpdated(val user: SessionUser?) : Message
        data object Deleting : Message
        data object DeleteFinished : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ActivityDetailsStore.Intent, Nothing, ActivityDetailsStore.State, Message, ActivityDetailsStore.Label>() {

        private var loadJob: Job? = null
        private var activitiesJob: Job? = null
        private var sessionJob: Job? = null
        private var pollingJob: Job? = null
        private val refreshMutex = Mutex()
        private var mutationFailuresObserved = false

        @Volatile
        private var activityInFeed = false

        override fun executeIntent(intent: ActivityDetailsStore.Intent) {
            when (intent) {
                is ActivityDetailsStore.Intent.Load -> load(intent.activityId)
                ActivityDetailsStore.Intent.Vote -> vote()
                ActivityDetailsStore.Intent.Unvote -> unvote()
                ActivityDetailsStore.Intent.Like -> like()
                ActivityDetailsStore.Intent.Dislike -> dislike()
                ActivityDetailsStore.Intent.Unlike -> unlike()
                ActivityDetailsStore.Intent.Delete -> delete()
                ActivityDetailsStore.Intent.StartPolling -> startPolling()
                ActivityDetailsStore.Intent.StopPolling -> {
                    pollingJob?.cancel()
                    pollingJob = null
                }

                ActivityDetailsStore.Intent.Back -> publish(ActivityDetailsStore.Label.Back)
            }
        }

        private fun load(activityId: String) {
            observeSession()
            observeMutationFailures()
            loadJob?.cancel()
            loadJob = scope.launch {
                dispatch(Message.Loading)
                when (val result = activityRepository.getEventById(activityId)) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.LoadFailed(result.error.toLoadState()))
                }
            }
            activitiesJob?.cancel()
            activitiesJob = scope.launch {
                activityRepository.observeActivities().collectLatest { list ->
                    val found = list.find { it.id == activityId }
                    activityInFeed = found != null
                    if (found != null) dispatch(Message.Loaded(found))
                }
            }
        }

        private fun observeSession() {
            if (sessionJob?.isActive == true) return
            sessionJob = scope.launch {
                observeSessionUseCase.state.collectLatest { sessionState ->
                    val user = (sessionState as? SessionState.Authenticated)?.user
                    dispatch(Message.UserUpdated(user))
                }
            }
        }

        private fun startPolling() {
            if (pollingJob?.isActive == true) return
            pollingJob = scope.launch {
                var failures = 0
                refresh()
                while (isActive) {
                    delay(pollDelay(failures))
                    failures = if (refresh()) 0 else failures + 1
                }
            }
        }

        private suspend fun refresh(): Boolean {
            if (!refreshMutex.tryLock()) return true
            try {
                val activityId = state().activity?.id
                return if (activityId != null && !activityInFeed) {
                    refreshActivity(activityId)
                } else {
                    refreshFeed()
                }
            } finally {
                refreshMutex.unlock()
            }
        }

        private suspend fun refreshFeed(): Boolean {
            when (val result = activityRepository.refresh()) {
                is DomainResult.Success -> return true
                is DomainResult.Failure -> {
                    dispatch(Message.ActionFailed(result.error.toUiText(ActivityOperation.DETAILS)))
                    return false
                }
            }
        }

        private suspend fun refreshActivity(activityId: String): Boolean {
            when (val result = activityRepository.getEventById(activityId)) {
                is DomainResult.Success -> {
                    dispatch(Message.Loaded(result.value))
                    return true
                }

                is DomainResult.Failure -> when (result.error) {
                    DomainError.NotFound -> {
                        dispatch(Message.ActivityRemoved)
                        return true
                    }

                    DomainError.Forbidden -> return true
                    else -> {
                        dispatch(Message.ActionFailed(result.error.toUiText(ActivityOperation.DETAILS)))
                        return false
                    }
                }
            }
        }

        private fun vote() {
            val activity = state().activity ?: return
            if (!state().canVote) return
            if (!activity.canChangeVote || activity.hasVoted) return
            act(ActivityOperation.VOTE) { activityRepository.vote(it) }
        }

        private fun unvote() {
            val activity = state().activity ?: return
            if (!state().canVote) return
            if (!activity.canWithdrawVote || !activity.hasVoted) return
            act(ActivityOperation.VOTE) { activityRepository.unvote(it) }
        }

        private fun like() {
            val activity = state().activity ?: return
            if (!state().canInteract || !activity.canLike || activity.likedByMe) return
            act(ActivityOperation.LIKE) { activityRepository.like(it) }
        }

        private fun dislike() {
            val activity = state().activity ?: return
            if (!state().canInteract || !activity.canLike || activity.dislikedByMe) return
            act(ActivityOperation.LIKE) { activityRepository.dislike(it) }
        }

        private fun unlike() {
            val activity = state().activity ?: return
            if (!state().canInteract || !activity.canLike || (!activity.likedByMe && !activity.dislikedByMe)) return
            act(ActivityOperation.LIKE) { activityRepository.unlike(it) }
        }

        private fun delete() {
            val activity = state().activity ?: return
            if (!state().canDelete || state().isDeleting) return
            dispatch(Message.Deleting)
            scope.launch {
                when (val result = activityRepository.delete(activity.id)) {
                    is DomainResult.Success -> {
                        dispatch(Message.DeleteFinished)
                        publish(ActivityDetailsStore.Label.Deleted)
                    }
                    is DomainResult.Failure -> {
                        dispatch(Message.DeleteFinished)
                        dispatch(Message.ActionFailed(result.error.toUiText(ActivityOperation.DELETE)))
                        refreshActivity(activity.id)
                    }
                }
            }
        }

        private fun act(
            operation: ActivityOperation,
            action: suspend (String) -> DomainResult<ActivityProposal>,
        ) {
            dispatch(Message.ClearError)
            scope.launch {
                val result = action(state().activity?.id ?: return@launch)
                if (result is DomainResult.Failure) {
                    dispatch(Message.ActionFailed(result.error.toUiText(operation)))
                }
            }
        }

        private fun observeMutationFailures() {
            if (mutationFailuresObserved) return
            mutationFailuresObserved = true
            scope.launch {
                activityRepository.mutationFailures.collect { failure ->
                    dispatch(Message.ActionFailed(failure.error.toUiText(failure.operation)))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ActivityDetailsStore.State, Message> {
        override fun ActivityDetailsStore.State.reduce(msg: Message): ActivityDetailsStore.State =
            when (msg) {
                Message.ClearError -> copy(error = null)
                Message.Loading -> copy(loadState = ActivityLoadState.Loading)
                is Message.Loaded -> copy(activity = msg.activity, loadState = ActivityLoadState.Idle, error = null)
                is Message.LoadFailed -> copy(loadState = msg.loadState)
                Message.ActivityRemoved -> copy(activity = null, loadState = ActivityLoadState.NotFound)
                is Message.ActionFailed -> copy(error = msg.error, errorId = errorId + 1)
                is Message.UserUpdated -> copy(currentUser = msg.user)
                Message.Deleting -> copy(isDeleting = true)
                Message.DeleteFinished -> copy(isDeleting = false)
            }
    }
}

private const val POLLING_INTERVAL_MS = 5_000L
private const val MAX_BACKOFF_STEPS = 3

private fun pollDelay(failures: Int): Long =
    POLLING_INTERVAL_MS shl failures.coerceIn(0, MAX_BACKOFF_STEPS)

private fun DomainError.toLoadState(): ActivityLoadState = when (this) {
    DomainError.NotFound -> ActivityLoadState.NotFound
    DomainError.Forbidden -> ActivityLoadState.Forbidden
    else -> ActivityLoadState.Failed(toUiText(ActivityOperation.DETAILS))
}
