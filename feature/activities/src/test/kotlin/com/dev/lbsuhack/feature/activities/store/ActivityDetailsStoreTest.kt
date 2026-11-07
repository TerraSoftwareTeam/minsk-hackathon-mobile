package com.dev.lbsuhack.feature.activities.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.feature.activities.domain.ActivityMutationFailure
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
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

    private fun createStore(activityId: String? = null): ActivityDetailsStore = ActivityDetailsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        activityRepository = activityRepository,
        observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
    ).create(activityId)

    @Test
    fun cachedCreatedActivityIsVisibleBeforeLoadingStarts() {
        val store = createStore("act-1")

        assertEquals(TestFixtures.ownProposal, store.state.activity)
        assertEquals(TestFixtures.pupil, store.state.currentUser)
        assertFalse(store.state.isLoading)
        store.dispose()
    }

    @Test
    fun ratingSwitchesBetweenDislikeLikeAndNoReaction() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()
        store.accept(ActivityDetailsStore.Intent.Dislike)
        runCurrent()
        assertEquals(-1, requireNotNull(store.state.activity).likesCount)
        assertTrue(requireNotNull(store.state.activity).dislikedByMe)
        store.accept(ActivityDetailsStore.Intent.Dislike)
        runCurrent()
        assertEquals(1, activityRepository.dislikeCalls)

        store.accept(ActivityDetailsStore.Intent.Like)
        runCurrent()
        assertEquals(1, requireNotNull(store.state.activity).likesCount)
        assertTrue(requireNotNull(store.state.activity).likedByMe)
        assertFalse(requireNotNull(store.state.activity).dislikedByMe)
        store.accept(ActivityDetailsStore.Intent.Dislike)
        runCurrent()
        assertEquals(-1, requireNotNull(store.state.activity).likesCount)
        assertFalse(requireNotNull(store.state.activity).likedByMe)
        assertTrue(requireNotNull(store.state.activity).dislikedByMe)
        store.accept(ActivityDetailsStore.Intent.Unlike)
        runCurrent()
        assertEquals(0, requireNotNull(store.state.activity).likesCount)
        assertFalse(requireNotNull(store.state.activity).likedByMe)
        assertFalse(requireNotNull(store.state.activity).dislikedByMe)
        store.dispose()
    }

    @Test
    fun ratingActionsAreUnavailableDuringBothModerationStages() = runTest(mainDispatcherRule.testDispatcher) {
        for (status in listOf(ActivityStatus.ON_MODERATION, ActivityStatus.ON_ADMIN_MODERATION)) {
            activityRepository.replace(listOf(TestFixtures.otherProposal.copy(status = status, likedByMe = true)))
            val store = createStore()
            store.accept(ActivityDetailsStore.Intent.Load("act-2"))
            runCurrent()
            store.accept(ActivityDetailsStore.Intent.Like)
            store.accept(ActivityDetailsStore.Intent.Dislike)
            store.accept(ActivityDetailsStore.Intent.Unlike)
            runCurrent()
            assertEquals(0, activityRepository.likeCalls)
            assertEquals(0, activityRepository.dislikeCalls)
            assertEquals(0, activityRepository.unlikeCalls)
            store.dispose()
        }
    }

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
    fun authorCanJoinButCannotLeaveOwnProposal() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.Vote)
        runCurrent()

        assertEquals(1, activityRepository.voteCalls)
        assertTrue(store.state.isAuthor)
        assertTrue(store.state.canVote)
        assertTrue(store.state.activity?.hasVoted == true)

        store.accept(ActivityDetailsStore.Intent.Unvote)
        runCurrent()

        assertEquals(0, activityRepository.unvoteCalls)
        assertTrue(store.state.activity?.hasVoted == true)
        store.dispose()
    }

    @Test
    fun authorCannotLeaveAfterVotingEnds() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.replace(
            listOf(TestFixtures.ownProposal.copy(status = ActivityStatus.FINALIZED, hasVoted = true)),
        )
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()
        store.accept(ActivityDetailsStore.Intent.Unvote)
        runCurrent()

        assertEquals(0, activityRepository.unvoteCalls)
        assertTrue(store.state.activity?.hasVoted == true)
        store.dispose()
    }

    @Test
    fun authorCanLikeAndUnlikeOwnProposal() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()

        store.accept(ActivityDetailsStore.Intent.Like)
        runCurrent()

        assertEquals(1, activityRepository.likeCalls)
        assertTrue(store.state.isAuthor)
        assertTrue(store.state.canInteract)
        assertTrue(store.state.activity?.likedByMe == true)

        store.accept(ActivityDetailsStore.Intent.Like)
        runCurrent()
        assertEquals(1, activityRepository.likeCalls)

        store.accept(ActivityDetailsStore.Intent.Unlike)
        runCurrent()
        assertEquals(1, activityRepository.unlikeCalls)
        assertFalse(store.state.activity?.likedByMe == true)
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

    @Test
    fun authorDeletesPendingActivity() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(ActivityDetailsStore.Intent.Load("act-1"))
        runCurrent()
        assertTrue(store.state.canDelete)

        store.accept(ActivityDetailsStore.Intent.Delete)
        runCurrent()

        assertEquals(listOf("act-1"), activityRepository.deletedIds)
        assertFalse(store.state.isDeleting)
        store.dispose()
    }

    @Test
    fun pupilCannotDeleteForeignActivity() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(ActivityDetailsStore.Intent.Load("act-2"))
        runCurrent()

        assertFalse(store.state.canDelete)
        store.accept(ActivityDetailsStore.Intent.Delete)
        runCurrent()

        assertTrue(activityRepository.deletedIds.isEmpty())
        store.dispose()
    }
}
