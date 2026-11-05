package com.dev.lbsuhack.feature.requests.data

import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HardcodedReviewRequestRepositoryTest {

    private val repository = HardcodedReviewRequestRepository()

    @Test
    fun `approve publication changes status`() = runTest {
        val result = repository.approvePublication("publication-1")

        assertTrue(result is ReviewRequest.Publication)
        assertEquals(ReviewStatus.APPROVED, result.status)
        assertNull(result.declineReason)
    }

    @Test
    fun `approve visit stores confirmed details`() = runTest {
        val result = repository.approveVisit(
            requestId = "visit-1",
            confirmedDate = "Суббота, 11:00",
            confirmedLocation = "Музей, главный вход",
        )

        assertTrue(result is ReviewRequest.Visit)
        assertEquals(ReviewStatus.APPROVED, result.status)
        assertEquals("Суббота, 11:00", (result as ReviewRequest.Visit).confirmedDate)
        assertEquals("Музей, главный вход", result.confirmedLocation)
    }

    @Test
    fun `decline stores trimmed reason`() = runTest {
        val result = repository.decline(
            requestId = "publication-1",
            reason = "  Нужно согласование  ",
        )

        assertEquals(ReviewStatus.DECLINED, result.status)
        assertEquals("Нужно согласование", result.declineReason)
    }

    @Test
    fun `decline without reason clears reason`() = runTest {
        val result = repository.decline(
            requestId = "visit-1",
            reason = "   ",
        )

        assertEquals(ReviewStatus.DECLINED, result.status)
        assertNull(result.declineReason)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `approve visit rejects blank details`() = runTest {
        repository.approveVisit(
            requestId = "visit-1",
            confirmedDate = " ",
            confirmedLocation = "Музей",
        )
    }
}
