package com.dev.lbsuhack.feature.createvote.data

import com.dev.lbsuhack.feature.createvote.domain.VoteDraft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardcodedVoteRepositoryTest {

    private val repository = HardcodedVoteRepository()

    @Test
    fun `demo events are available`() = runTest {
        val events = repository.observeEvents().first()

        assertEquals(4, events.size)
        assertTrue(events.all { it.participants.isNotEmpty() })
    }

    @Test
    fun `create vote trims fields and stores the vote`() = runTest {
        val created = repository.createVote(
            VoteDraft(
                eventId = "event-quiz",
                title = "  Какой формат квиза выбрать?  ",
                description = "  Обсудим на встрече  ",
                deadline = "  Пятница, 18:00  ",
                participantIds = setOf("quiz-pupil-1", "quiz-pupil-2"),
            ),
        )

        assertEquals("Какой формат квиза выбрать?", created.title)
        assertEquals("Обсудим на встрече", created.description)
        assertEquals("Пятница, 18:00", created.deadline)
        assertEquals(setOf("quiz-pupil-1", "quiz-pupil-2"), created.participantIds)
        assertEquals(listOf(created), repository.observeVotes().first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create vote rejects participants from another event`() = runTest {
        repository.createVote(
            VoteDraft(
                eventId = "event-quiz",
                title = "Вопрос",
                description = "",
                deadline = "Пятница",
                participantIds = setOf("tennis-pupil-1"),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create vote rejects blank title`() = runTest {
        repository.createVote(
            VoteDraft(
                eventId = "event-quiz",
                title = "   ",
                description = "",
                deadline = "Пятница",
                participantIds = setOf("quiz-pupil-1"),
            ),
        )
    }
}
