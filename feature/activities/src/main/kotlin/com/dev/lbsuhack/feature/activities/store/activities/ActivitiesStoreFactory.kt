package com.dev.lbsuhack.feature.activities.store.activities

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.store.ActivityOperation
import com.dev.lbsuhack.feature.activities.store.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class ActivitiesStoreFactory(
    private val storeFactory: StoreFactory,
    private val activityRepository: ActivityRepository,
    private val observeSessionUseCase: ObserveSessionUseCase,
) {

    fun create(): ActivitiesStore =
        object : ActivitiesStore,
            Store<ActivitiesStore.Intent, ActivitiesStore.State, ActivitiesStore.Label> by storeFactory.create(
                name = "ActivitiesStore",
                initialState = ActivitiesStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data object ClearError : Message
        data object Loading : Message
        data object Loaded : Message
        data class Failed(val error: UiText) : Message
        data class ActivitiesLoaded(val activities: List<ActivityProposal>) : Message
        data class UserUpdated(val user: SessionUser?) : Message
        data class CategorySelected(val category: ActivityCategory?) : Message
        data class TabSelected(val tab: ActivityTab) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ActivitiesStore.Intent, Nothing, ActivitiesStore.State, Message, ActivitiesStore.Label>() {

        private var observing = false
        private var refreshJob: Job? = null
        private var pollingJob: Job? = null
        private val refreshMutex = Mutex()
        private val mutating = mutableSetOf<String>()

        override fun executeIntent(intent: ActivitiesStore.Intent) {
            when (intent) {
                ActivitiesStore.Intent.Refresh -> load(silent = false)
                ActivitiesStore.Intent.StartPolling -> startPolling()
                ActivitiesStore.Intent.StopPolling -> stopPolling()
                is ActivitiesStore.Intent.SelectCategory -> dispatch(Message.CategorySelected(intent.category))
                is ActivitiesStore.Intent.SelectTab -> dispatch(Message.TabSelected(intent.tab))
                is ActivitiesStore.Intent.ClickActivity -> publish(ActivitiesStore.Label.OpenDetails(intent.activityId))
                ActivitiesStore.Intent.ClickCreate -> openCreate()
                ActivitiesStore.Intent.ClickProfile -> publish(ActivitiesStore.Label.OpenProfile)
                is ActivitiesStore.Intent.Vote -> vote(intent.activityId)
                is ActivitiesStore.Intent.Unvote -> unvote(intent.activityId)
                is ActivitiesStore.Intent.Like -> like(intent.activityId)
                is ActivitiesStore.Intent.Unlike -> unlike(intent.activityId)
            }
        }

        private fun openCreate() {
            val role = state().currentUser?.role
            if (role == UserRole.PUPIL) {
                publish(ActivitiesStore.Label.OpenCreate)
            } else if (role != null) {
                publish(ActivitiesStore.Label.OpenRequests(role))
            }
        }

        private fun observe() {
            if (observing) return
            observing = true
            scope.launch {
                observeSessionUseCase.state.collectLatest { sessionState ->
                    val user = (sessionState as? SessionState.Authenticated)?.user
                    dispatch(Message.UserUpdated(user))
                }
            }
            scope.launch {
                activityRepository.observeActivities().collectLatest { activitiesList ->
                    dispatch(Message.ActivitiesLoaded(activitiesList))
                }
            }
        }

        private fun load(silent: Boolean) {
            observe()
            if (refreshJob?.isActive == true) return
            refreshJob = scope.launch { refresh(silent) }
        }

        private fun startPolling() {
            observe()
            if (pollingJob?.isActive == true) return
            pollingJob = scope.launch {
                var failures = 0
                refresh(silent = state().activities.isNotEmpty())
                while (isActive) {
                    delay(pollDelay(failures))
                    failures = if (refresh(silent = true)) 0 else failures + 1
                }
            }
        }

        private fun stopPolling() {
            pollingJob?.cancel()
            pollingJob = null
        }

        private suspend fun refresh(silent: Boolean): Boolean {
            if (!refreshMutex.tryLock()) return true
            try {
                if (!silent) dispatch(Message.Loading)
                when (val result = activityRepository.refresh()) {
                    is DomainResult.Success -> {
                        dispatch(Message.Loaded)
                        return true
                    }

                    is DomainResult.Failure -> {
                        if (!silent || state().activities.isEmpty()) {
                            dispatch(Message.Failed(result.error.toUiText(ActivityOperation.LIST)))
                        }
                        return false
                    }
                }
            } finally {
                refreshMutex.unlock()
            }
        }

        private fun vote(activityId: String) {
            val activity = state().activities.find { it.id == activityId } ?: return
            if (!state().canVoteIn(activity)) return
            if (activity.hasVoted) return
            act(activityId, ActivityOperation.VOTE) { activityRepository.vote(it) }
        }

        private fun unvote(activityId: String) {
            val activity = state().activities.find { it.id == activityId } ?: return
            if (!state().canUnvoteIn(activity)) return
            if (!activity.hasVoted) return
            act(activityId, ActivityOperation.VOTE) { activityRepository.unvote(it) }
        }

        private fun like(activityId: String) {
            val activity = state().activities.find { it.id == activityId } ?: return
            if (!state().canLikeIn(activity)) return
            if (activity.likedByMe) return
            act(activityId, ActivityOperation.LIKE) { activityRepository.like(it) }
        }

        private fun unlike(activityId: String) {
            val activity = state().activities.find { it.id == activityId } ?: return
            if (!state().canLikeIn(activity)) return
            if (!activity.likedByMe) return
            act(activityId, ActivityOperation.LIKE) { activityRepository.unlike(it) }
        }

        private fun act(
            activityId: String,
            operation: ActivityOperation,
            action: suspend (String) -> DomainResult<*>,
        ) {
            if (!mutating.add(activityId)) return
            dispatch(Message.ClearError)
            scope.launch {
                try {
                    val result = action(activityId)
                    if (result is DomainResult.Failure) {
                        dispatch(Message.Failed(result.error.toUiText(operation)))
                    }
                } finally {
                    mutating.remove(activityId)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ActivitiesStore.State, Message> {
        override fun ActivitiesStore.State.reduce(msg: Message): ActivitiesStore.State =
            when (msg) {
                Message.ClearError -> copy(error = null)
                Message.Loading -> copy(isLoading = true, error = null)
                Message.Loaded -> copy(isLoading = false, error = null)
                is Message.Failed -> copy(isLoading = false, error = msg.error, errorId = errorId + 1)
                is Message.ActivitiesLoaded -> copy(activities = msg.activities, isLoading = false)
                is Message.UserUpdated -> copy(currentUser = msg.user)
                is Message.CategorySelected -> copy(selectedCategory = msg.category)
                is Message.TabSelected -> copy(selectedTab = msg.tab)
            }
    }
}

private const val POLLING_INTERVAL_MS = 5_000L
private const val MAX_BACKOFF_STEPS = 3

private fun pollDelay(failures: Int): Long =
    POLLING_INTERVAL_MS shl failures.coerceIn(0, MAX_BACKOFF_STEPS)
