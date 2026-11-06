package com.dev.lbsuhack.feature.activities.store.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.toUiText
import kotlinx.coroutines.Job
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ActivityDetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val activityRepository: ActivityRepository,
    private val observeSessionUseCase: ObserveSessionUseCase,
) {

    fun create(): ActivityDetailsStore =
        object : ActivityDetailsStore,
            Store<ActivityDetailsStore.Intent, ActivityDetailsStore.State, ActivityDetailsStore.Label> by storeFactory.create(
                name = "ActivityDetailsStore",
                initialState = ActivityDetailsStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Message {
        data object ClearError : Message
        data object Loading : Message
        data class Failed(val error: UiText) : Message
        data class Loaded(val activity: ActivityProposal?) : Message
        data class UserUpdated(val user: SessionUser?) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ActivityDetailsStore.Intent, Nothing, ActivityDetailsStore.State, Message, ActivityDetailsStore.Label>() {

        private var loadJob: Job? = null
        private var sessionJob: Job? = null
        private var voting = false

        override fun executeIntent(intent: ActivityDetailsStore.Intent) {
            when (intent) {
                is ActivityDetailsStore.Intent.Load -> load(intent.activityId)
                ActivityDetailsStore.Intent.Vote -> vote()
                ActivityDetailsStore.Intent.Back -> publish(ActivityDetailsStore.Label.Back)
            }
        }

        private fun load(activityId: String) {
            sessionJob?.cancel()
            sessionJob = scope.launch {
                observeSessionUseCase.state.collectLatest { sessionState ->
                    val user = (sessionState as? SessionState.Authenticated)?.user
                    dispatch(Message.UserUpdated(user))
                }
            }

            loadJob?.cancel()
            loadJob = scope.launch {
                dispatch(Message.Loading)
                when (val result = activityRepository.getEventById(activityId)) {
                    is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                    is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                }

                activityRepository.observeActivities().collectLatest { list ->
                    val found = list.find { it.id == activityId }
                    if (found != null) {
                        dispatch(Message.Loaded(found))
                    }
                }
            }
        }

        private fun vote() {
            val activity = state().activity ?: return
            if (voting || activity.hasVoted) return
            voting = true
            dispatch(Message.ClearError)
            scope.launch {
                try {
                    when (val result = activityRepository.vote(activity.id)) {
                        is DomainResult.Success -> dispatch(Message.Loaded(result.value))
                        is DomainResult.Failure -> dispatch(Message.Failed(result.error.toUiText()))
                    }
                } finally {
                    voting = false
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ActivityDetailsStore.State, Message> {
        override fun ActivityDetailsStore.State.reduce(msg: Message): ActivityDetailsStore.State =
            when (msg) {
                Message.ClearError -> copy(error = null)
                Message.Loading -> copy(isLoading = true, error = null)
                is Message.Failed -> copy(isLoading = false, error = msg.error)
                is Message.Loaded -> copy(activity = msg.activity, isLoading = false)
                is Message.UserUpdated -> copy(currentUser = msg.user)
            }
    }
}
