package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.core.network.generated.model.CreateEventDto
import com.dev.lbsuhack.core.network.generated.model.EventLikeResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventVoteResponseDto
import com.dev.lbsuhack.core.network.generated.model.VotingEventResponseDto
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.util.Locale

class EventMapperTest {

    @Test
    fun categoryToDtoMapsAllValues() {
        for (category in ActivityCategory.entries) {
            val dto = category.toDto()
            assertEquals(category.name, dto.name)
        }
    }

    @Test
    fun votingEventCategoryToDomainMapsAllValues() {
        for (dtoCategory in VotingEventResponseDto.Category.entries) {
            val domain = dtoCategory.toDomain()
            assertEquals(if (dtoCategory.name == "VOLUNTEER") "OTHER" else dtoCategory.name, domain.name)
        }
    }

    @Test
    fun eventResponseCategoryToDomainMapsAllValues() {
        for (dtoCategory in EventResponseDto.Category.entries) {
            val domain = dtoCategory.toDomain()
            assertEquals(if (dtoCategory.name == "VOLUNTEER") "OTHER" else dtoCategory.name, domain.name)
        }
    }

    @Test
    fun scopeToDtoMapsAllValues() {
        for (scope in ActivityScope.entries) {
            val dto = scope.toDto()
            assertEquals(scope.name, dto.name)
        }
    }

    @Test
    fun votingEventScopeToDomainMapsAllValues() {
        for (dtoScope in VotingEventResponseDto.Scope.entries) {
            val domain = dtoScope.toDomain()
            assertEquals(dtoScope.name, domain.name)
        }
    }

    @Test
    fun eventResponseScopeToDomainMapsAllValues() {
        for (dtoScope in EventResponseDto.Scope.entries) {
            val domain = dtoScope.toDomain()
            assertEquals(dtoScope.name, domain.name)
        }
    }

    @Test
    fun votingEventStatusToDomainMapsAllValues() {
        for (status in VotingEventResponseDto.Status.entries) {
            val domain = status.toDomain()
            assertEquals(status.name, domain.name)
        }
    }

    @Test
    fun eventResponseStatusToDomainMapsAllValues() {
        for (status in EventResponseDto.Status.entries) {
            val domain = status.toDomain()
            assertEquals(status.name, domain.name)
        }
    }

    @Test
    fun eventVoteStatusToDomainMapsAllValues() {
        for (status in EventVoteResponseDto.Status.entries) {
            val domain = status.toDomain()
            assertEquals(status.name, domain.name)
        }
    }

    @Test
    fun withVoteResultAddsAndRemovesVoter() {
        val initial = ActivityProposal(
            id = "1",
            title = "Test",
            description = "Desc",
            category = ActivityCategory.SPORTS,
            status = ActivityStatus.ON_VOTE,
            voteThreshold = 10,
            votesCount = 1,
            hasVoted = false,
            authorName = "Author",
            voters = listOf("Другой Пользователь"),
        )
        val voteResult = EventVoteResponseDto(
            eventId = "1",
            votesCount = 2,
            voteThreshold = 10,
            status = EventVoteResponseDto.Status.ON_VOTE,
        )

        val voted = initial.withVoteResult(voteResult, voted = true, voterName = "Новый Голосующий")
        assertEquals(2, voted.votesCount)
        assertTrue(voted.hasVoted)
        assertEquals(listOf("Другой Пользователь", "Новый Голосующий"), voted.voters)

        val unvoteResult = EventVoteResponseDto(
            eventId = "1",
            votesCount = 1,
            voteThreshold = 10,
            status = EventVoteResponseDto.Status.ON_VOTE,
        )
        val unvoted = voted.withVoteResult(unvoteResult, voted = false, voterName = "Новый Голосующий")
        assertEquals(1, unvoted.votesCount)
        assertFalse(unvoted.hasVoted)
        assertEquals(listOf("Другой Пользователь"), unvoted.voters)
    }

    @Test
    fun withLikeResultUpdatesLikesCountAndLikedByMe() {
        val initial = ActivityProposal(
            id = "1",
            title = "Test",
            description = "Desc",
            category = ActivityCategory.SPORTS,
            status = ActivityStatus.ON_VOTE,
            voteThreshold = 10,
            votesCount = 0,
            hasVoted = false,
            authorName = "Author",
            likesCount = 0,
            likedByMe = false,
        )
        val liked = initial.withLikeResult(EventLikeResponseDto(eventId = "1", liked = true, likesCount = 1))
        assertEquals(1, liked.likesCount)
        assertTrue(liked.likedByMe)

        val disliked = liked.withLikeResult(EventLikeResponseDto(eventId = "1", liked = false, disliked = true, likesCount = -1))
        assertEquals(-1, disliked.likesCount)
        assertFalse(disliked.likedByMe)
        assertTrue(disliked.dislikedByMe)

        val unliked = disliked.withLikeResult(EventLikeResponseDto(eventId = "1", liked = false, disliked = false, likesCount = 0))
        assertEquals(0, unliked.likesCount)
        assertFalse(unliked.likedByMe)
        assertFalse(unliked.dislikedByMe)
    }

    @Test
    fun toDisplayDateTimeFormatsValidIsoDate() {
        val iso = "2026-11-20T14:30:00Z"
        val formatted = iso.toDisplayDateTime(zone = ZoneId.of("UTC"), locale = Locale.ENGLISH)
        assertEquals("20 November, 14:30", formatted)

        val invalid = "not-a-date"
        assertEquals("not-a-date", invalid.toDisplayDateTime())
    }
}
