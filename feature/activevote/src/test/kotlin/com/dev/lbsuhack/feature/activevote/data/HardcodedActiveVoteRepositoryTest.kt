package com.dev.lbsuhack.feature.activevote.data

import com.dev.lbsuhack.feature.activevote.domain.ActiveVote
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardcodedActiveVoteRepositoryTest {

    private val repository = HardcodedActiveVoteRepository()

    @Test
    fun `demo votes are available`() = runTest {
        val votes = repository.observeVotes().first()

        assertTrue(votes.isNotEmpty())
        assertTrue(votes.all { it.options.size >= 2 })
    }

    @Test
    fun `demo votes contain events and voters`() = runTest {
        val votes = repository.observeVotes().first()

        assertTrue(votes.all { it.events.isNotEmpty() })
        assertTrue(votes.all { it.users.isNotEmpty() })
    }

    @Test
    fun `submit vote stores the acceptance`() = runTest {
        val acceptance = repository.submitVote(
            voteId = "active-quiz",
            optionId = "quiz-team",
        )

        assertEquals("active-quiz", acceptance.voteId)
        assertEquals("quiz-team", acceptance.optionId)
        assertEquals(listOf(acceptance), repository.observeResults().first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `submit vote rejects an unknown option`() = runTest {
        repository.submitVote(
            voteId = "active-quiz",
            optionId = "unknown",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `submit vote rejects a repeated vote`() = runTest {
        repository.submitVote("active-quiz", "quiz-team")
        repository.submitVote("active-quiz", "quiz-solo")
    }

    @Test
    fun `loaded vote matches the requested id`() = runTest {
        val vote = repository.observeVotes().first().first { it.id == "active-tennis" }

        assertEquals("Турнир по настольному теннису", vote.eventTitle)
    }
}
