package com.dev.lbsuhack.feature.activities.store.activities

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
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
                ActivitiesStore.Intent.Load, ActivitiesStore.Intent.Refresh -> load()
                is ActivitiesStore.Intent.SelectCategory -> dispatch(Message.CategorySelected(intent.category))
                is ActivitiesStore.Intent.SelectTab -> dispatch(Message.TabSelected(intent.tab))
                is ActivitiesStore.Intent.ClickActivity -> publish(ActivitiesStore.Label.OpenDetails(intent.activityId))
                ActivitiesStore.Intent.ClickCreate -> publish(ActivitiesStore.Label.OpenCreate)
                ActivitiesStore.Intent.ClickProfile -> publish(ActivitiesStore.Label.OpenProfile)
                is ActivitiesStore.Intent.Vote -> vote(intent.activityId)
            }
        }

        private fun load() {
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

            scope.launch {
                dispatch(Message.Loading)
                activityRepository.refresh()
            }
        }

        private fun vote(activityId: String) {
            scope.launch {
                activityRepository.vote(activityId)
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
