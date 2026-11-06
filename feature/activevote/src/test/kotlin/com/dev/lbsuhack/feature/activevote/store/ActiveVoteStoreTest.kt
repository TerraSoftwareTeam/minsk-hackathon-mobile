package com.dev.lbsuhack.feature.activevote.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activevote.R
import com.dev.lbsuhack.feature.activevote.data.HardcodedActiveVoteRepository
import com.dev.lbsuhack.feature.activevote.domain.VoteAcceptance
import com.dev.lbsuhack.feature.activevote.store.activevote.ActiveVoteStore
import com.dev.lbsuhack.feature.activevote.store.activevote.ActiveVoteStoreFactory
import com.dev.lbsuhack.feature.activevote.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveVoteStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): ActiveVoteStore =
        ActiveVoteStoreFactory(
            storeFactory = DefaultStoreFactory(),
            activeVoteRepository = HardcodedActiveVoteRepository(),
        ).create()

    @Test
    fun `load exposes the requested vote`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActiveVoteStore.Intent.Load("active-quiz"))
        runCurrent()

        assertFalse(store.state.isLoading)
        assertEquals("active-quiz", store.state.vote?.id)
        assertEquals(3, store.state.vote?.options?.size)
    }

    @Test
    fun `selecting an option updates the state`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(ActiveVoteStore.Intent.Load("active-quiz"))
        runCurrent()

        store.accept(ActiveVoteStore.Intent.SelectOption("quiz-team"))
        runCurrent()

        assertEquals("quiz-team", store.state.selectedOptionId)
    }

    @Test
    fun `selecting an unknown option is ignored`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(ActiveVoteStore.Intent.Load("active-quiz"))
        runCurrent()

        store.accept(ActiveVoteStore.Intent.SelectOption("unknown"))
        runCurrent()

        assertNull(store.state.selectedOptionId)
    }

    @Test
    fun `submit without an option reports an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(ActiveVoteStore.Intent.Load("active-quiz"))
            runCurrent()

            store.accept(ActiveVoteStore.Intent.Submit)
            runCurrent()

            assertEquals(
                UiText.Resource(R.string.active_vote_error_option),
                store.state.error,
            )
        }

    @Test
    fun `submit publishes the acceptance and marks the vote as done`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(ActiveVoteStore.Intent.Load("active-quiz"))
            runCurrent()
            store.accept(ActiveVoteStore.Intent.SelectOption("quiz-team"))
            runCurrent()

            store.labels.test {
                store.accept(ActiveVoteStore.Intent.Submit)
                runCurrent()

                assertEquals(
                    ActiveVoteStore.Label.VoteAccepted(
                        VoteAcceptance(voteId = "active-quiz", optionId = "quiz-team"),
                    ),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }

            assertTrue(store.state.hasVoted)
            assertFalse(store.state.isSubmitting)
        }

    @Test
    fun `dismiss error clears the error from state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(ActiveVoteStore.Intent.Load("active-quiz"))
            runCurrent()

            store.accept(ActiveVoteStore.Intent.Submit)
            runCurrent()
            assertEquals(
                UiText.Resource(R.string.active_vote_error_option),
                store.state.error,
            )

            store.accept(ActiveVoteStore.Intent.DismissError)
            runCurrent()
            assertNull(store.state.error)
        }

    @Test
    fun `load exposes vote events and voters`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(ActiveVoteStore.Intent.Load("active-quiz"))
            runCurrent()

            val vote = store.state.vote
            assertTrue(vote != null && vote.events.isNotEmpty())
            assertTrue(vote != null && vote.users.isNotEmpty())
        }
}
