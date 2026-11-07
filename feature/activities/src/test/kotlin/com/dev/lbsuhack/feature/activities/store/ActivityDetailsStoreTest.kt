package com.dev.lbsuhack.feature.activities.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.feature.activities.domain.ActivityMutationFailure
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStore
import com.dev.lbsuhack.feature.activities.store.details.ActivityDetailsStoreFactory
import com.dev.lbsuhack.feature.activities.store.details.ActivityLoadState
import com.dev.lbsuhack.feature.activities.util.FakeActivityRepository
import com.dev.lbsuhack.feature.activities.util.FakeSessionRepository
import com.dev.lbsuhack.feature.activities.util.MainDispatcherRule
import com.dev.lbsuhack.feature.activities.util.TestFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ActivityDetailsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = FakeSessionRepository(
        initialState = SessionState.Authenticated(TestFixtures.pupil),
    )

    private val activityRepository = FakeActivityRepository(
        listOf(TestFixtures.ownProposal, TestFixtures.otherProposal),
    )

    private fun createStore(): ActivityDetailsStore = ActivityDetailsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        activityRepository = activityRepository,
        observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun loadFillsStateWithActivityAndUser() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        assertEquals("act-2", store.state.activity?.id)
        assertEquals(TestFixtures.pupil, store.state.currentUser)
        assertEquals(ActivityLoadState.Idle, store.state.loadState)
        store.dispose()
    }

    @Test
    fun missingEventProducesNotFoundLoadState() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.getEventResult = DomainResult.Failure(DomainError.NotFound)
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("missing"))
        runCurrent()

        assertEquals(ActivityLoadState.NotFound, store.state.loadState)
        assertNull(store.state.activity)
        store.dispose()
    }

    @Test
    fun forbiddenEventProducesForbiddenLoadState() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.getEventResult = DomainResult.Failure(DomainError.Forbidden)
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-private"))
        runCurrent()

        assertEquals(ActivityLoadState.Forbidden, store.state.loadState)
        assertNull(store.state.activity)
        store.dispose()
    }

    @Test
    fun networkFailureProducesFailedLoadState() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.getEventResult = DomainResult.Failure(DomainError.Network)
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-private"))
        runCurrent()

        val loadState = store.state.loadState
        assertTrue(loadState is ActivityLoadState.Failed)
        assertNotNull((loadState as ActivityLoadState.Failed).error)
        store.dispose()
    }

    @Test
    fun authorCannotVoteOnOwnProposal() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.Vote)
        runCurrent()

        assertEquals(0, activityRepository.voteCalls)
        assertTrue(store.state.isAuthor)
        assertFalse(store.state.canInteract)
        store.dispose()
    }

    @Test
    fun authorCannotLikeOwnProposal() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.Like)
        runCurrent()

        assertEquals(0, activityRepository.likeCalls)
        assertTrue(store.state.isAuthor)
        assertFalse(store.state.canInteract)
        store.dispose()
    }

    @Test
    fun pupilVotesForOtherProposal() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.Vote)
        runCurrent()

        assertEquals(1, activityRepository.voteCalls)
        assertTrue(store.state.activity?.hasVoted == true)
        store.dispose()
    }

    @Test
    fun rapidVoteAndUnvoteEndsUpUnvoted() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.Vote)
        runCurrent()
        assertTrue(store.state.activity?.hasVoted == true)

        store.accept(ActivityDetailsStore.Intent.Unvote)
        runCurrent()
        assertFalse(store.state.activity?.hasVoted == true)
        assertEquals(1, activityRepository.voteCalls)
        assertEquals(1, activityRepository.unvoteCalls)
        store.dispose()
    }

    @Test
    fun mutationFailureExposesError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        activityRepository.emitMutationFailure(
            ActivityMutationFailure("act-2", ActivityOperation.LIKE, DomainError.Network),
        )
        runCurrent()

        assertNotNull(store.state.error)
        store.dispose()
    }

    @Test
    fun failedVoteExposesError() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.voteResult = DomainResult.Failure(DomainError.Forbidden)
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.Vote)
        runCurrent()

        assertEquals(
            com.dev.lbsuhack.core.localization.text.UiText.Resource(com.dev.lbsuhack.feature.activities.R.string.activity_error_forbidden_vote),
            store.state.error,
        )
        store.dispose()
    }

    @Test
    fun loadedClearsPreviousError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        activityRepository.voteResult = DomainResult.Failure(DomainError.Forbidden)
        store.accept(ActivityDetailsStore.Intent.Vote)
        runCurrent()
        assertNotNull(store.state.error)

        activityRepository.voteResult = null
        store.accept(ActivityDetailsStore.Intent.Vote)
        runCurrent()
        assertNull(store.state.error)
        store.dispose()
    }

    @Test
    fun activityRemovedFromFeedClearsCard() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()
        assertNotNull(store.state.activity)

        activityRepository.replace(emptyList())
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.StartPolling)
        runCurrent()

        assertNull(store.state.activity)
        assertEquals(ActivityLoadState.NotFound, store.state.loadState)
        store.accept(ActivityDetailsStore.Intent.StopPolling)
        store.dispose()
    }

    @Test
    fun failedPollingKeepsLoadedCardAndReportsError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        activityRepository.refreshResult = DomainResult.Failure(DomainError.Network)
        store.accept(ActivityDetailsStore.Intent.StartPolling)
        runCurrent()

        assertEquals("act-2", store.state.activity?.id)
        assertNotNull(store.state.error)
        store.accept(ActivityDetailsStore.Intent.StopPolling)
        store.dispose()
    }
}
