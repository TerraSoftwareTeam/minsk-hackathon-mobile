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
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
import com.dev.lbsuhack.feature.activities.store.toUiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
                initialState = ActivitiesStore.State(
                    currentUser = (observeSessionUseCase.state.value as? SessionState.Authenticated)?.user,
                ),
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
        private var mutationFailuresObserved = false
        private var refreshJob: Job? = null
        private var pollingJob: Job? = null
        private val refreshMutex = Mutex()

        override fun executeIntent(intent: ActivitiesStore.Intent) {
            when (intent) {
                ActivitiesStore.Intent.Refresh -> load(silent = false)
                ActivitiesStore.Intent.StartPolling -> startPolling()
                ActivitiesStore.Intent.StopPolling -> stopPolling()
                is ActivitiesStore.Intent.SelectCategory -> dispatch(Message.CategorySelected(intent.category))
                is ActivitiesStore.Intent.SelectTab -> dispatch(Message.TabSelected(intent.tab))
                ActivitiesStore.Intent.ResetFilters -> {
                    dispatch(Message.CategorySelected(null))
                    dispatch(Message.TabSelected(ActivityTab.ALL))
                }
                is ActivitiesStore.Intent.ClickActivity -> publish(ActivitiesStore.Label.OpenDetails(intent.activityId))
                ActivitiesStore.Intent.ClickCreate -> openCreate()
                ActivitiesStore.Intent.ClickProfile -> publish(ActivitiesStore.Label.OpenProfile)
                is ActivitiesStore.Intent.Vote -> vote(intent.activityId)
                is ActivitiesStore.Intent.Unvote -> unvote(intent.activityId)
                is ActivitiesStore.Intent.Like -> like(intent.activityId)
                is ActivitiesStore.Intent.Dislike -> dislike(intent.activityId)
                is ActivitiesStore.Intent.Unlike -> unlike(intent.activityId)
            }
        }

        private fun openCreate() {
            if (state().currentUser != null) publish(ActivitiesStore.Label.OpenCreate)
        }

        private fun observe() {
            if (observing) return
            observing = true
            observeMutationFailures()
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
                val cached = activityRepository.observeActivities().first()
                if (cached.isNotEmpty()) dispatch(Message.ActivitiesLoaded(cached))
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
                        dispatch(Message.ActivitiesLoaded(activityRepository.observeActivities().first()))
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

        private fun dislike(activityId: String) {
            val activity = state().activities.find { it.id == activityId } ?: return
            if (!state().canLikeIn(activity) || activity.dislikedByMe) return
            act(activityId, ActivityOperation.LIKE) { activityRepository.dislike(it) }
        }

        private fun unlike(activityId: String) {
            val activity = state().activities.find { it.id == activityId } ?: return
            if (!state().canLikeIn(activity)) return
            if (!activity.likedByMe && !activity.dislikedByMe) return
            act(activityId, ActivityOperation.LIKE) { activityRepository.unlike(it) }
        }

        private fun act(
            activityId: String,
            operation: ActivityOperation,
            action: suspend (String) -> DomainResult<*>,
        ) {
            dispatch(Message.ClearError)
            scope.launch {
                val result = action(activityId)
                if (result is DomainResult.Failure) {
                    dispatch(Message.Failed(result.error.toUiText(operation)))
                }
            }
        }

        private fun observeMutationFailures() {
            if (mutationFailuresObserved) return
            mutationFailuresObserved = true
            scope.launch {
                activityRepository.mutationFailures.collect { failure ->
                    dispatch(Message.Failed(failure.error.toUiText(failure.operation)))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ActivitiesStore.State, Message> {
        override fun ActivitiesStore.State.reduce(msg: Message): ActivitiesStore.State =
            when (msg) {
                Message.ClearError -> copy(error = null)
                Message.Loading -> copy(isLoading = true, error = null)
                Message.Loaded -> copy(isLoading = false, hasLoaded = true, error = null)
                is Message.Failed -> copy(isLoading = false, error = msg.error, errorId = errorId + 1)
                is Message.ActivitiesLoaded -> copy(
                    activities = msg.activities,
                    hasLoaded = hasLoaded || msg.activities.isNotEmpty(),
                )
                is Message.UserUpdated -> if (currentUser?.id != msg.user?.id || currentUser?.role != msg.user?.role) {
                    ActivitiesStore.State(currentUser = msg.user)
                } else copy(currentUser = msg.user)
                is Message.CategorySelected -> copy(selectedCategory = msg.category)
                is Message.TabSelected -> copy(selectedTab = msg.tab)
            }
    }
}

private const val POLLING_INTERVAL_MS = 1_000L
private const val MAX_BACKOFF_STEPS = 3

private fun pollDelay(failures: Int): Long =
    POLLING_INTERVAL_MS shl failures.coerceIn(0, MAX_BACKOFF_STEPS)
