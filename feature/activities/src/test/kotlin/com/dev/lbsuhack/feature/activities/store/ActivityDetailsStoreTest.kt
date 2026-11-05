package com.dev.lbsuhack.feature.activities.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.data.ActivityRepositoryImpl
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStore
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStoreFactory
import com.dev.lbsuhack.feature.activities.util.FakeSessionRepository
import com.dev.lbsuhack.feature.activities.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityDetailsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionUser = SessionUser(
        id = 101L,
        name = "Никита Ковалев",
        email = "nikita@schools.by",
        pupilClass = "10-А",
    )

    private val sessionRepository = FakeSessionRepository(
        initialState = SessionState.Authenticated(sessionUser),
    )

    private val activityRepository = ActivityRepositoryImpl()

    private fun createStore(): ActivityDetailsStore = ActivityDetailsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        activityRepository = activityRepository,
        observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun loadFillsStateWithActivity() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()

        assertFalse(store.state.isLoading)
        assertNotNull(store.state.activity)
        assertEquals("act-1", store.state.activity?.id)
        assertTrue(store.state.isAuthor)
    }

    @Test
    fun toggleJoinTogglesParticipation() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-4"))
        runCurrent()

        val initialJoined = store.state.isJoined

        store.accept(ActivityDetailsStore.Intent.ToggleJoin)
        runCurrent()

        assertEquals(!initialJoined, store.state.isJoined)
    }

    @Test
    fun simulateTransitionToAdminUpdatesStatus() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.SimulateTransitionToAdmin)
        runCurrent()

        assertEquals(ActivityStatus.PENDING_ADMIN, store.state.activity?.status)
    }

    @Test
    fun simulateApproveUpdatesStatusAndLocation() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        store.accept(
            ActivityDetailsStore.Intent.SimulateApprove(
                time = "Суббота, 14:00",
                location = "Актовый зал",
            ),
        )
        runCurrent()

        assertEquals(ActivityStatus.APPROVED, store.state.activity?.status)
        assertEquals("Суббота, 14:00", store.state.activity?.confirmedTime)
        assertEquals("Актовый зал", store.state.activity?.confirmedLocation)
    }

    @Test
    fun backPublishesLabel() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.labels.test {
            store.accept(ActivityDetailsStore.Intent.Back)
            assertEquals(ActivityDetailsStore.Label.Back, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
