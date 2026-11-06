package com.dev.lbsuhack.feature.createvote.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.createvote.R
import com.dev.lbsuhack.feature.createvote.data.HardcodedVoteRepository
import com.dev.lbsuhack.feature.createvote.store.createvote.CreateVoteStore
import com.dev.lbsuhack.feature.createvote.store.createvote.CreateVoteStoreFactory
import com.dev.lbsuhack.feature.createvote.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateVoteStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = HardcodedVoteRepository()

    private fun createStore(): CreateVoteStore =
        CreateVoteStoreFactory(
            storeFactory = DefaultStoreFactory(),
            voteRepository = repository,
        ).create()

    @Test
    fun `load exposes events`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(CreateVoteStore.Intent.Load)
        runCurrent()

        assertFalse(store.state.isLoading)
        assertEquals(4, store.state.events.size)
    }

    @Test
    fun `selecting event prefills title and exposes participants`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(CreateVoteStore.Intent.Load)
            runCurrent()

            store.accept(CreateVoteStore.Intent.SelectEvent("event-quiz"))
            runCurrent()

            assertEquals("event-quiz", store.state.selectedEventId)
            assertEquals("Школьный квиз «Что? Где? Когда?»", store.state.title)
            assertEquals(6, store.state.availableParticipants.size)
        }

    @Test
    fun `selecting another event keeps only valid participants`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(CreateVoteStore.Intent.Load)
            runCurrent()
            store.accept(CreateVoteStore.Intent.SelectEvent("event-quiz"))
            runCurrent()
            store.accept(CreateVoteStore.Intent.ToggleParticipant("quiz-pupil-1"))
            runCurrent()

            store.accept(CreateVoteStore.Intent.SelectEvent("event-tennis"))
            runCurrent()

            assertTrue(store.state.selectedParticipantIds.isEmpty())
        }

    @Test
    fun `toggling a participant updates the selection`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(CreateVoteStore.Intent.Load)
            runCurrent()
            store.accept(CreateVoteStore.Intent.SelectEvent("event-quiz"))
            runCurrent()

            store.accept(CreateVoteStore.Intent.ToggleParticipant("quiz-pupil-1"))
            runCurrent()
            assertEquals(setOf("quiz-pupil-1"), store.state.selectedParticipantIds)

            store.accept(CreateVoteStore.Intent.ToggleParticipant("quiz-pupil-1"))
            runCurrent()
            assertTrue(store.state.selectedParticipantIds.isEmpty())
        }

    @Test
    fun `toggle all selects and clears every participant`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(CreateVoteStore.Intent.Load)
            runCurrent()
            store.accept(CreateVoteStore.Intent.SelectEvent("event-quiz"))
            runCurrent()

            store.accept(CreateVoteStore.Intent.ToggleAllParticipants)
            runCurrent()
            assertEquals(6, store.state.selectedParticipantIds.size)
            assertTrue(store.state.allParticipantsSelected)

            store.accept(CreateVoteStore.Intent.ToggleAllParticipants)
            runCurrent()
            assertTrue(store.state.selectedParticipantIds.isEmpty())
        }

    @Test
    fun `submit without event reports an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(CreateVoteStore.Intent.Load)
            runCurrent()

            store.accept(CreateVoteStore.Intent.Submit)
            runCurrent()

            assertEquals(
                UiText.Resource(R.string.create_vote_error_event),
                store.state.error,
            )
        }

    @Test
    fun `submit without deadline reports an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(CreateVoteStore.Intent.Load)
            runCurrent()
            store.accept(CreateVoteStore.Intent.SelectEvent("event-quiz"))
            runCurrent()

            store.accept(CreateVoteStore.Intent.Submit)
            runCurrent()

            assertEquals(
                UiText.Resource(R.string.create_vote_error_deadline),
                store.state.error,
            )
        }

    @Test
    fun `submit without participants reports an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(CreateVoteStore.Intent.Load)
            runCurrent()
            store.accept(CreateVoteStore.Intent.SelectEvent("event-quiz"))
            store.accept(CreateVoteStore.Intent.ChangeDeadline("Пятница, 18:00"))
            runCurrent()

            store.accept(CreateVoteStore.Intent.Submit)
            runCurrent()

            assertEquals(
                UiText.Resource(R.string.create_vote_error_participants),
                store.state.error,
            )
        }

    @Test
    fun `successful submit creates a vote and publishes the label`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(CreateVoteStore.Intent.Load)
            runCurrent()
            store.accept(CreateVoteStore.Intent.SelectEvent("event-quiz"))
            store.accept(CreateVoteStore.Intent.ChangeDeadline("Пятница, 18:00"))
            store.accept(CreateVoteStore.Intent.ToggleParticipant("quiz-pupil-1"))
            runCurrent()

            store.labels.test {
                store.accept(CreateVoteStore.Intent.Submit)
                runCurrent()

                assertEquals(CreateVoteStore.Label.Created, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            val votes = repository.observeVotes().first()
            assertEquals(1, votes.size)
            assertEquals("event-quiz", votes.single().eventId)
            assertEquals(setOf("quiz-pupil-1"), votes.single().participantIds)
            assertFalse(store.state.isSubmitting)
        }
}
