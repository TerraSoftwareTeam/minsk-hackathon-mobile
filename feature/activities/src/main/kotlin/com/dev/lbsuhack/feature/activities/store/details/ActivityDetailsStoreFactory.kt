package com.dev.lbsuhack.feature.activities.store.details

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.domain.ActivityParticipant
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
        data object Loading : Message
        data class Loaded(val activity: ActivityProposal?) : Message
        data class UserUpdated(val user: SessionUser?) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ActivityDetailsStore.Intent, Nothing, ActivityDetailsStore.State, Message, ActivityDetailsStore.Label>() {

        override fun executeIntent(intent: ActivityDetailsStore.Intent) {
            when (intent) {
                is ActivityDetailsStore.Intent.Load -> load(intent.activityId)
                ActivityDetailsStore.Intent.ToggleJoin -> toggleJoin()
                ActivityDetailsStore.Intent.SimulateTransitionToAdmin -> simulateAdminTransition()
                is ActivityDetailsStore.Intent.SimulateApprove -> simulateApprove(intent.time, intent.location)
                ActivityDetailsStore.Intent.Back -> publish(ActivityDetailsStore.Label.Back)
            }
        }

        private fun load(activityId: String) {
            scope.launch {
                observeSessionUseCase.state.collectLatest { sessionState ->
                    val user = (sessionState as? SessionState.Authenticated)?.user
                    dispatch(Message.UserUpdated(user))
                }
            }

            scope.launch {
                dispatch(Message.Loading)
                activityRepository.observeActivities().collectLatest { list ->
                    val found = list.find { it.id == activityId }
                    dispatch(Message.Loaded(found))
                }
            }
        }

        private fun toggleJoin() {
            val user = state().currentUser ?: return
            val activity = state().activity ?: return

            scope.launch {
                if (activity.isJoined(user.id)) {
                    activityRepository.leaveActivity(activity.id, user.id)
                } else {
                    activityRepository.joinActivity(
                        activityId = activity.id,
                        participant = ActivityParticipant(
                            pupilId = user.id,
                            name = user.name,
                            pupilClass = user.pupilClass ?: "10-А",
                        ),
                    )
                }
            }
        }

        private fun simulateAdminTransition() {
            val activity = state().activity ?: return
            scope.launch {
                activityRepository.transitionToAdmin(activity.id)
            }
        }

        private fun simulateApprove(time: String, location: String) {
            val activity = state().activity ?: return
            scope.launch {
                activityRepository.approveActivity(
                    activityId = activity.id,
                    confirmedTime = time,
                    confirmedLocation = location,
                )
            }
        }
    }

    private object ReducerImpl : Reducer<ActivityDetailsStore.State, Message> {
        override fun ActivityDetailsStore.State.reduce(msg: Message): ActivityDetailsStore.State =
            when (msg) {
                Message.Loading -> copy(isLoading = true, error = null)
                is Message.Loaded -> copy(activity = msg.activity, isLoading = false)
                is Message.UserUpdated -> copy(currentUser = msg.user)
            }
    }
}
