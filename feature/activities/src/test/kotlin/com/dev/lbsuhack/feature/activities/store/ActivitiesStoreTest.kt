package com.dev.lbsuhack.feature.activities.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityMutationFailure
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStoreFactory
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import com.dev.lbsuhack.feature.activities.util.FakeActivityRepository
import com.dev.lbsuhack.feature.activities.util.FakeSessionRepository
import com.dev.lbsuhack.feature.activities.util.MainDispatcherRule
import com.dev.lbsuhack.feature.activities.util.TestFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = FakeSessionRepository(
        initialState = SessionState.Authenticated(TestFixtures.pupil),
    )

    private val activityRepository = FakeActivityRepository(
        listOf(TestFixtures.ownProposal, TestFixtures.otherProposal),
    )

    private fun createStore(): ActivitiesStore = ActivitiesStoreFactory(
        storeFactory = DefaultStoreFactory(),
        activityRepository = activityRepository,
        observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun pollingFillsStateWithActivitiesAndUser() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        assertFalse(store.state.isLoading)
        assertEquals(2, store.state.activities.size)
        assertEquals(TestFixtures.pupil, store.state.currentUser)
        store.dispose()
    }

    @Test
    fun selectCategoryFiltersActivities() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.SelectCategory(ActivityCategory.INTELLECTUAL))
        runCurrent()

        assertEquals(ActivityCategory.INTELLECTUAL, store.state.selectedCategory)
        assertEquals(listOf("act-2"), store.state.filteredActivities.map { it.id })
        store.dispose()
    }

    @Test
    fun selectTabFiltersActivities() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.SelectTab(ActivityTab.MY_PROPOSALS))
        runCurrent()

        assertEquals(ActivityTab.MY_PROPOSALS, store.state.selectedTab)
        assertEquals(listOf("act-1"), store.state.filteredActivities.map { it.id })
        store.dispose()
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
    fun clickCreateForPupilPublishesOpenCreateLabel() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.labels.test {
            store.accept(ActivitiesStore.Intent.ClickCreate)
            assertEquals(ActivitiesStore.Label.OpenCreate, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        store.dispose()
    }

    @Test
    fun authorCannotVoteOnOwnProposal() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Vote("act-1"))
        runCurrent()

        assertEquals(0, activityRepository.voteCalls)
        assertFalse(store.state.activities.first { it.id == "act-1" }.hasVoted)
        store.dispose()
    }

    @Test
    fun authorCannotLikeOwnProposal() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Like("act-1"))
        runCurrent()

        assertEquals(0, activityRepository.likeCalls)
        assertFalse(store.state.activities.first { it.id == "act-1" }.likedByMe)
        store.dispose()
    }

    @Test
    fun voteIsRejectedWhenStatusDoesNotAllowVoting() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.replace(
            listOf(TestFixtures.otherProposal.copy(status = ActivityStatus.FINALIZED)),
        )
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Vote("act-2"))
        runCurrent()

        assertEquals(0, activityRepository.voteCalls)
        store.dispose()
    }

    @Test
    fun pupilVotesForOtherProposal() = runTest(mainDispatcherRule.testDispatcher) {        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Vote("act-2"))
        runCurrent()

        assertEquals(1, activityRepository.voteCalls)
        assertTrue(store.state.activities.first { it.id == "act-2" }.hasVoted)
        store.dispose()
    }

    @Test
    fun unvoteIsRejectedWhenProposalWasNotVoted() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Unvote("act-2"))
        runCurrent()

        assertEquals(0, activityRepository.unvoteCalls)
        store.dispose()
    }

    @Test
    fun failedRefreshExposesErrorAndSuccessClearsIt() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.refreshResult = DomainResult.Failure(DomainError.Network)
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Refresh)
        runCurrent()

        assertFalse(store.state.isLoading)
        assertNotNull(store.state.error)

        activityRepository.refreshResult = DomainResult.Success(Unit)
        store.accept(ActivitiesStore.Intent.Refresh)
        runCurrent()

        assertFalse(store.state.isLoading)
        assertNull(store.state.error)
        store.dispose()
    }

    @Test
    fun failedVoteExposesError() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.voteResult = DomainResult.Failure(DomainError.Forbidden)
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Vote("act-2"))
        runCurrent()

        assertEquals(
            com.dev.lbsuhack.core.localization.text.UiText.Resource(com.dev.lbsuhack.feature.activities.R.string.activity_error_forbidden_vote),
            store.state.error,
        )
        store.dispose()
    }

    @Test
    fun loadedClearsPreviousError() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.voteResult = DomainResult.Failure(DomainError.Forbidden)
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Vote("act-2"))
        runCurrent()
        assertNotNull(store.state.error)

        activityRepository.refreshResult = DomainResult.Success(Unit)
        store.accept(ActivitiesStore.Intent.Refresh)
        runCurrent()

        assertNull(store.state.error)
        store.dispose()
    }

    @Test
    fun likeAndUnlikeUpdateRating() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Like("act-2"))
        runCurrent()
        assertEquals(1, store.state.activities.first { it.id == "act-2" }.likesCount)
        assertTrue(store.state.activities.first { it.id == "act-2" }.likedByMe)

        store.accept(ActivitiesStore.Intent.Unlike("act-2"))
        runCurrent()
        assertEquals(0, store.state.activities.first { it.id == "act-2" }.likesCount)
        assertEquals(1, activityRepository.likeCalls)
        assertEquals(1, activityRepository.unlikeCalls)
        store.dispose()
    }

    @Test
    fun rapidVoteAndUnvoteEndsUpUnvoted() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        store.accept(ActivitiesStore.Intent.Vote("act-2"))
        runCurrent()
        assertTrue(store.state.activities.first { it.id == "act-2" }.hasVoted)

        store.accept(ActivitiesStore.Intent.Unvote("act-2"))
        runCurrent()
        assertFalse(store.state.activities.first { it.id == "act-2" }.hasVoted)
        assertEquals(1, activityRepository.voteCalls)
        assertEquals(1, activityRepository.unvoteCalls)
        store.dispose()
    }

    @Test
    fun mutationFailureExposesError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()

        activityRepository.emitMutationFailure(
            ActivityMutationFailure("act-2", ActivityOperation.VOTE, DomainError.Network),
        )
        runCurrent()

        assertNotNull(store.state.error)
        store.dispose()
    }

    @Test
    fun pollingRefreshesImmediatelyAndThenEveryFiveSeconds() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()
        assertEquals(1, activityRepository.refreshCalls)

        advanceTimeBy(5_001)
        assertEquals(2, activityRepository.refreshCalls)
        advanceTimeBy(5_000)
        assertEquals(3, activityRepository.refreshCalls)

        store.accept(ActivitiesStore.Intent.StopPolling)
        advanceTimeBy(30_000)
        assertEquals(3, activityRepository.refreshCalls)
        store.dispose()
    }

    @Test
    fun pollingBacksOffAfterFailures() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.refreshResult = DomainResult.Failure(DomainError.Network)
        val store = createStore()

        store.accept(ActivitiesStore.Intent.StartPolling)
        runCurrent()
        assertEquals(1, activityRepository.refreshCalls)

        advanceTimeBy(5_001)
        assertEquals(2, activityRepository.refreshCalls)

        advanceTimeBy(10_001)
        assertEquals(3, activityRepository.refreshCalls)
        store.dispose()
    }
}
