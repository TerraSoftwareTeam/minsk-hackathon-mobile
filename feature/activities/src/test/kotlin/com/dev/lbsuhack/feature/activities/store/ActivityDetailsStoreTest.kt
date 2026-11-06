package com.dev.lbsuhack.feature.activities.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStore
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStoreFactory
import com.dev.lbsuhack.feature.activities.util.FakeActivityRepository
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
        id = "101",
        name = "Никита Ковалев",
        schoolsById = "schools-101",
        schoolName = "School #1",
        pupilClass = "10-А",
    )

    private val sessionRepository = FakeSessionRepository(
        initialState = SessionState.Authenticated(sessionUser),
    )

    private val initialProposals = listOf(
        ActivityProposal(
            id = "act-1",
            title = "Футбольный матч",
            description = "Игра на поле",
            category = ActivityCategory.SPORTS,
            scope = ActivityScope.SCHOOL,
            status = ActivityStatus.ON_VOTE,
            voteThreshold = 15,
            votesCount = 10,
            hasVoted = false,
            startsAt = "2026-11-21T10:00:00.000Z",
            votingClosesAt = "2026-11-19T10:00:00.000Z",
            authorId = "101",
            authorSchoolsById = "schools-101",
            authorName = "Никита Ковалев",
            authorClass = "10-А",
        ),
    )

    private val activityRepository = FakeActivityRepository(initialProposals)

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
    fun voteUpdatesParticipation() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()

        assertFalse(store.state.hasVoted)

        store.accept(ActivityDetailsStore.Intent.Vote)
        runCurrent()

        assertTrue(store.state.hasVoted)
        assertEquals(11, store.state.activity?.votesCount)
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
