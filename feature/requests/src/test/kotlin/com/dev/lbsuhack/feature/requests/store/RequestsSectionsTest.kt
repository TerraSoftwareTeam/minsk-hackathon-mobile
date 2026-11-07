package com.dev.lbsuhack.feature.requests.store

import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.store.requests.AccessState
import com.dev.lbsuhack.feature.requests.store.requests.RequestsSectionKind
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab
import org.junit.Assert.assertEquals
import org.junit.Test

class RequestsSectionsTest {

    private fun request(id: String, status: ReviewStatus) = ReviewRequest(
        id = id,
        title = id,
        description = "",
        proposedDate = null,
        authorName = "Автор",
        status = status,
        audience = RequestAudience.TEACHER,
    )

    private fun state(tab: RequestsTab) = RequestsStore.State(
        audience = RequestAudience.TEACHER,
        accessState = AccessState.Granted,
        selectedTab = tab,
        requests = listOf(
            request("approved", ReviewStatus.APPROVED),
            request("pending-1", ReviewStatus.PENDING_TEACHER),
            request("declined", ReviewStatus.DECLINED),
            request("pending-2", ReviewStatus.PENDING_TEACHER),
        ),
    )

    @Test
    fun allTabShowsPendingFirstThenProcessed() {
        val sections = state(RequestsTab.ALL).sections

        assertEquals(listOf(RequestsSectionKind.PENDING, RequestsSectionKind.PROCESSED), sections.map { it.kind })
        assertEquals(listOf("pending-1", "pending-2"), sections[0].requests.map { it.id })
        assertEquals(listOf("approved", "declined"), sections[1].requests.map { it.id })
    }

    @Test
    fun singleTabProducesASingleSection() {
        assertEquals(listOf(RequestsSectionKind.PENDING), state(RequestsTab.PENDING).sections.map { it.kind })
        assertEquals(listOf(RequestsSectionKind.PROCESSED), state(RequestsTab.PROCESSED).sections.map { it.kind })
    }

    @Test
    fun noRequestsMeansNoSections() {
        assertEquals(emptyList<Any>(), RequestsStore.State(audience = RequestAudience.TEACHER, accessState = AccessState.Granted).sections)
    }
}
