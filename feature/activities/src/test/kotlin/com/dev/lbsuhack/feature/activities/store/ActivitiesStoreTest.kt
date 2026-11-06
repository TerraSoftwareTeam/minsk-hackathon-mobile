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
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStoreFactory
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import com.dev.lbsuhack.feature.activities.util.FakeActivityRepository
import com.dev.lbsuhack.feature.activities.util.FakeSessionRepository
import com.dev.lbsuhack.feature.activities.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionUser = SessionUser(
        id = "101",
        name = "Никита Ковалев",
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
            authorName = "Никита Ковалев",
            authorClass = "10-А",
        ),
        ActivityProposal(
            id = "act-2",
            title = "Шахматный турнир",
            description = "Быстрые шахматы",
            category = ActivityCategory.INTELLECTUAL,
            scope = ActivityScope.GRADE,
            status = ActivityStatus.FINALIZED,
            voteThreshold = 10,
            votesCount = 10,
            hasVoted = true,
            startsAt = "2026-11-21T12:00:00.000Z",
            votingClosesAt = "2026-11-19T12:00:00.000Z",
            authorId = "202",
            authorName = "Мария",
            authorClass = "10-Б",
        ),
    )

    private val activityRepository = FakeActivityRepository(initialProposals)

    private fun createStore(): ActivitiesStore = ActivitiesStoreFactory(
        storeFactory = DefaultStoreFactory(),
        activityRepository = activityRepository,
        observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun loadFillsStateWithActivitiesAndUser() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()

        assertFalse(store.state.isLoading)
        assertTrue(store.state.activities.isNotEmpty())
        assertEquals(sessionUser, store.state.currentUser)
    }

    @Test
    fun selectCategoryFiltersActivities() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()

        store.accept(ActivitiesStore.Intent.SelectCategory(ActivityCategory.SPORTS))
        runCurrent()

        assertEquals(ActivityCategory.SPORTS, store.state.selectedCategory)
        assertTrue(store.state.filteredActivities.all { it.category == ActivityCategory.SPORTS })
    }

    @Test
    fun selectTabFiltersActivities() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()

        store.accept(ActivitiesStore.Intent.SelectTab(ActivityTab.MY_PROPOSALS))
        runCurrent()

        assertEquals(ActivityTab.MY_PROPOSALS, store.state.selectedTab)
        assertTrue(store.state.filteredActivities.all { it.authorId == sessionUser.id })
    }

    @Test
    fun clickActivityPublishesOpenDetailsLabel() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.labels.test {
            store.accept(ActivitiesStore.Intent.ClickActivity("act-1"))
            assertEquals(ActivitiesStore.Label.OpenDetails("act-1"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun voteCallsRepositoryAndUpdatesState() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Vote("act-1"))
        runCurrent()

        val itemAfter = store.state.activities.find { it.id == "act-1" }
        assertTrue(itemAfter?.hasVoted == true)
        assertEquals(11, itemAfter?.votesCount)
    }
}
