package com.dev.lbsuhack.feature.activities.store.activities

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityParticipant
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
        data object Loading : Message
        data class ActivitiesLoaded(val activities: List<ActivityProposal>) : Message
        data class UserUpdated(val user: SessionUser?) : Message
        data class CategorySelected(val category: ActivityCategory?) : Message
        data class TabSelected(val tab: ActivityTab) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ActivitiesStore.Intent, Nothing, ActivitiesStore.State, Message, ActivitiesStore.Label>() {

        override fun executeIntent(intent: ActivitiesStore.Intent) {
            when (intent) {
                ActivitiesStore.Intent.Load, ActivitiesStore.Intent.Refresh -> startObserving()
                is ActivitiesStore.Intent.SelectCategory -> dispatch(Message.CategorySelected(intent.category))
                is ActivitiesStore.Intent.SelectTab -> dispatch(Message.TabSelected(intent.tab))
                is ActivitiesStore.Intent.ClickActivity -> publish(ActivitiesStore.Label.OpenDetails(intent.activityId))
                ActivitiesStore.Intent.ClickCreate -> publish(ActivitiesStore.Label.OpenCreate)
                ActivitiesStore.Intent.ClickProfile -> publish(ActivitiesStore.Label.OpenProfile)
                is ActivitiesStore.Intent.ToggleJoin -> toggleJoin(intent.activityId)
            }
        }

        private fun startObserving() {
            scope.launch {
                observeSessionUseCase.state.collectLatest { sessionState ->
                    val user = (sessionState as? SessionState.Authenticated)?.user
                    dispatch(Message.UserUpdated(user))
                }
            }

            scope.launch {
                dispatch(Message.Loading)
                activityRepository.observeActivities().collectLatest { activitiesList ->
                    dispatch(Message.ActivitiesLoaded(activitiesList))
                }
            }
        }

        private fun toggleJoin(activityId: String) {
            val user = state().currentUser ?: return
            val activity = state().activities.find { it.id == activityId } ?: return

            scope.launch {
                if (activity.isJoined(user.id)) {
                    activityRepository.leaveActivity(activityId, user.id)
                } else {
                    activityRepository.joinActivity(
                        activityId = activityId,
                        participant = ActivityParticipant(
                            pupilId = user.id,
                            name = user.name,
                            pupilClass = user.pupilClass ?: "10-А",
                        ),
                    )
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ActivitiesStore.State, Message> {
        override fun ActivitiesStore.State.reduce(msg: Message): ActivitiesStore.State =
            when (msg) {
                Message.Loading -> copy(isLoading = true, error = null)
                is Message.ActivitiesLoaded -> copy(activities = msg.activities, isLoading = false)
                is Message.UserUpdated -> copy(currentUser = msg.user)
                is Message.CategorySelected -> copy(selectedCategory = msg.category)
                is Message.TabSelected -> copy(selectedTab = msg.tab)
            }
    }
}
