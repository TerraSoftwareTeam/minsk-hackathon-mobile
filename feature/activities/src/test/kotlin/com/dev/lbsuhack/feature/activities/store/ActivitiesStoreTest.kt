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
import kotlinx.coroutines.test.advanceTimeBy
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
            authorSchoolsById = "schools-202",
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
        assertTrue(store.state.filteredActivities.all { it.isCreatedBy(sessionUser.schoolsById) })
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
    @Test
    fun failedRefreshStopsLoadingAndExposesError() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.refreshResult = com.dev.lbsuhack.core.domain.DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Network)
        val store = createStore()
        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()
        assertFalse(store.state.isLoading)
        assertTrue(store.state.error != null)
        activityRepository.refreshResult = com.dev.lbsuhack.core.domain.DomainResult.Success(Unit)
        store.accept(ActivitiesStore.Intent.Refresh)
        runCurrent()
        assertFalse(store.state.isLoading)
        assertEquals(null, store.state.error)
        store.dispose()
    }

    @Test
    fun failedVoteExposesError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(ActivitiesStore.Intent.Vote("missing"))
        runCurrent()
        assertTrue(store.state.error != null)
        store.dispose()
    }
    @Test
    fun ownershipUsesSchoolsIdWhenBackendIdsDiffer() {
        val proposal = initialProposals.first().copy(authorId = "another-backend-id")
        val state = ActivitiesStore.State(
            activities = listOf(proposal, initialProposals.last()),
            currentUser = sessionUser,
            selectedTab = ActivityTab.MY_PROPOSALS,
        )
        assertTrue(proposal.isCreatedBy(sessionUser.schoolsById))
        assertEquals(listOf(proposal), state.filteredActivities)
        assertEquals(1, state.myProposalsCount)
    }

    @Test
    fun missingOrDifferentSchoolsIdDoesNotClaimOwnership() {
        val proposal = initialProposals.first()
        assertFalse(proposal.isCreatedBy(null))
        assertFalse(proposal.isCreatedBy(""))
        assertFalse(proposal.isCreatedBy("other-school-user"))
        assertFalse(proposal.copy(authorSchoolsById = null).isCreatedBy(null))
    }

    @Test
    fun pollingRefreshesEveryTwoSecondsUntilStopped() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()
        assertEquals(1, activityRepository.refreshCalls)

        advanceTimeBy(2_001)
        assertEquals(2, activityRepository.refreshCalls)
        advanceTimeBy(2_000)
        assertEquals(3, activityRepository.refreshCalls)

        store.accept(ActivitiesStore.Intent.StopPolling)
        advanceTimeBy(10_000)
        assertEquals(3, activityRepository.refreshCalls)
        store.dispose()
    }

    @Test
    fun pollingWithLoadedDataStaysSilentOnFailure() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()
        assertEquals(2, store.state.activities.size)

        activityRepository.refreshResult =
            com.dev.lbsuhack.core.domain.DomainResult.Failure(com.dev.lbsuhack.core.domain.DomainError.Network)
        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()
        advanceTimeBy(4_001)

        assertFalse(store.state.isLoading)
        assertEquals(null, store.state.error)
        assertEquals(2, store.state.activities.size)
        store.accept(ActivitiesStore.Intent.StopPolling)
        store.dispose()
    }

    @Test
    fun ownProposalIsRecognisedByUserIdEvenWithoutSchoolsById() {
        val own = initialProposals.first().copy(authorSchoolsById = null)
        assertTrue(own.isCreatedBy(sessionUser.id, null))
        assertFalse(own.isCreatedBy("other", null))
    }
}
