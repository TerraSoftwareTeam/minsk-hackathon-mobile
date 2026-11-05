package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityParticipant
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityRepositoryImplTest {

    private val repository = ActivityRepositoryImpl()

    @Test
    fun initialActivitiesAreLoaded() = runTest {
        val initial = repository.getActivities()
        assertTrue(initial.isNotEmpty())
    }

    @Test
    fun createProposalAddsNewItemWithModeratingStatus() = runTest {
        val created = repository.createProposal(
            title = "Шахматный клуб",
            category = ActivityCategory.BOARD_GAMES,
            description = "Играем в блиц",
            targetClass = "9–11",
            minParticipants = 4,
            maxParticipants = 8,
            preferenceNotes = "Нужны доски",
            authorId = "1",
            authorName = "Никита",
            authorClass = "10-А",
        )

        assertEquals("Шахматный клуб", created.title)
        assertEquals(ActivityStatus.MODERATING, created.status)
        assertEquals(1, created.participants.size)

        val found = repository.getActivityById(created.id)
        assertNotNull(found)
        assertEquals(created.id, found?.id)
    }

    @Test
    fun joinAndLeaveActivityUpdatesParticipants() = runTest {
        val proposal = repository.createProposal(
            title = "Киновечер",
            category = ActivityCategory.CINEMA,
            description = "Смотрим классику",
            targetClass = "10-11",
            minParticipants = 2,
            maxParticipants = 5,
            preferenceNotes = "",
            authorId = "1",
            authorName = "Никита",
            authorClass = "10-А",
        )

        val newParticipant = ActivityParticipant("2", "Мария", "10-Б")
        val joined = repository.joinActivity(proposal.id, newParticipant)
        assertEquals(2, joined.participants.size)
        assertTrue(joined.isJoined("2"))

        val left = repository.leaveActivity(proposal.id, "2")
        assertEquals(1, left.participants.size)
        assertTrue(!left.isJoined("2"))
    }

    @Test
    fun transitionToAdminAndApproveChangesStatus() = runTest {
        val proposal = repository.createProposal(
            title = "Настолки",
            category = ActivityCategory.BOARD_GAMES,
            description = "Игры",
            targetClass = "Все",
            minParticipants = 2,
            maxParticipants = 6,
            preferenceNotes = "",
            authorId = "1",
            authorName = "Никита",
            authorClass = "10-А",
        )

        val toAdmin = repository.transitionToAdmin(proposal.id)
        assertEquals(ActivityStatus.PENDING_ADMIN, toAdmin.status)

        val approved = repository.approveActivity(
            activityId = proposal.id,
            confirmedTime = "Суббота, 12:00",
            confirmedLocation = "Кабинет 204",
        )
        assertEquals(ActivityStatus.APPROVED, approved.status)
        assertEquals("Суббота, 12:00", approved.confirmedTime)
        assertEquals("Кабинет 204", approved.confirmedLocation)
    }
}
