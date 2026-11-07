package com.dev.lbsuhack.feature.requests.data

import com.dev.lbsuhack.core.network.generated.model.EventResponseDto
import com.dev.lbsuhack.core.network.generated.model.EventUserResponseDto
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.OffsetDateTime

class EventReviewMapperTest {

    private fun dto(
        id: String = "e-1",
        name: String = "Экскурсия в музей",
        description: String? = "Описание",
        status: EventResponseDto.Status = EventResponseDto.Status.ON_MODERATION,
        declineReason: String? = null,
        startsAt: String? = "2026-11-21T10:00:00Z",
        initiator: EventUserResponseDto = EventUserResponseDto("Иван", "Иванов"),
        voters: List<EventUserResponseDto> = emptyList(),
    ): EventResponseDto = EventResponseDto(
        id = id,
        name = name,
        description = description,
        scope = EventResponseDto.Scope.CLASS,
        category = EventResponseDto.Category.CULTURAL,
        status = status,
        declineReason = declineReason,
        voteThreshold = 15,
        startsAt = startsAt,
        votingClosesAt = "2026-12-06T00:00:00Z",
        successProbability = BigDecimal("0.5"),
        imageUrl = null,
        likesCount = 0,
        initiator = initiator,
        teacher = null,
        voters = voters,
        votesCount = voters.size,
        createdAt = "2026-11-06T00:00:00Z",
        updatedAt = "2026-11-06T00:00:00Z",
    )

    @Test
    fun `every backend status maps to a review status`() {
        val expectations = mapOf(
            EventResponseDto.Status.ON_MODERATION to ReviewStatus.PENDING_TEACHER,
            EventResponseDto.Status.ON_ADMIN_MODERATION to ReviewStatus.PENDING_ADMIN,
            EventResponseDto.Status.ON_VOTE to ReviewStatus.APPROVED,
            EventResponseDto.Status.FINALIZED to ReviewStatus.APPROVED,
            EventResponseDto.Status.DECLINED to ReviewStatus.DECLINED,
            EventResponseDto.Status.DECLINED_BY_ADMIN to ReviewStatus.DECLINED,
            EventResponseDto.Status.EXPIRED to ReviewStatus.DECLINED,
        )

        expectations.forEach { (backendStatus, reviewStatus) ->
            assertEquals(reviewStatus, dto(status = backendStatus).toDomain(RequestAudience.TEACHER).status)
        }
    }

    @Test
    fun `author name is built from the initiator`() {
        val request = dto(initiator = EventUserResponseDto("Иван", "Иванов")).toDomain(RequestAudience.TEACHER)

        assertEquals("Иван Иванов", request.authorName)
    }

    @Test
    fun `author name keeps only the first name when the surname is blank`() {
        val request = dto(initiator = EventUserResponseDto("Иван", " ")).toDomain(RequestAudience.TEACHER)

        assertEquals("Иван", request.authorName)
    }

    @Test
    fun `proposed date is parsed from the backend timestamp`() {
        val request = dto(startsAt = "2026-11-21T10:00:00Z").toDomain(RequestAudience.TEACHER)

        assertEquals(OffsetDateTime.parse("2026-11-21T10:00:00Z"), request.proposedDate)
    }

    @Test
    fun `missing date becomes null`() {
        val request = dto(startsAt = null).toDomain(RequestAudience.TEACHER)

        assertNull(request.proposedDate)
    }

    @Test
    fun `broken date becomes null instead of failing`() {
        val request = dto(startsAt = "не дата").toDomain(RequestAudience.TEACHER)

        assertNull(request.proposedDate)
    }

    @Test
    fun `empty voter list stays empty`() {
        val request = dto(voters = emptyList()).toDomain(RequestAudience.TEACHER)

        assertTrue(request.voters.isEmpty())
        assertEquals(0, request.votesCount)
    }

    @Test
    fun `voters are mapped to full names`() {
        val request = dto(
            voters = listOf(EventUserResponseDto("Пётр", "Петров"), EventUserResponseDto("Иван", "Иванов")),
        ).toDomain(RequestAudience.TEACHER)

        assertEquals(listOf("Пётр Петров", "Иван Иванов"), request.voters)
        assertEquals(2, request.votesCount)
    }

    @Test
    fun `missing description becomes an empty string`() {
        val request = dto(description = null).toDomain(RequestAudience.TEACHER)

        assertEquals("", request.description)
    }

    @Test
    fun `decline reason is preserved`() {
        val request = dto(declineReason = "Нет мест").toDomain(RequestAudience.ADMIN)

        assertEquals("Нет мест", request.declineReason)
        assertEquals(RequestAudience.ADMIN, request.audience)
    }
}
