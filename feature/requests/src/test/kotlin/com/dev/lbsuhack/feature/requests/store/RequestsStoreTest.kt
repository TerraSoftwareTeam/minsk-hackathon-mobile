package com.dev.lbsuhack.feature.requests.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.data.HardcodedReviewRequestRepository
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.store.requests.RequestDialog
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStoreFactory
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab
import com.dev.lbsuhack.feature.requests.util.FakeSessionRepository
import com.dev.lbsuhack.feature.requests.util.MainDispatcherRule
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
class RequestsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val teacher = SessionUser(
        id = "teacher-1",
        name = "Ирина Петровна",
        role = UserRole.TEACHER,
        pupilClass = "10-А",
    )

    private val admin = SessionUser(
        id = "admin-1",
        name = "Сергей Николаевич",
        role = UserRole.ORGANIZATOR,
    )

    private val pupil = SessionUser(
        id = "pupil-1",
        name = "Никита Ковалев",
        role = UserRole.PUPIL,
        pupilClass = "10-А",
    )

    private fun createStore(user: SessionUser): RequestsStore {
        val sessionRepository = FakeSessionRepository(
            initialState = SessionState.Authenticated(user),
        )
        return RequestsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            requestRepository = HardcodedReviewRequestRepository(),
            observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
        ).create()
    }

    @Test
    fun `teacher route shows publication queue`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(teacher)

        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        assertTrue(store.state.canReview)
        assertEquals(RequestAudience.TEACHER, store.state.audience)
        assertTrue(store.state.visibleRequests.all { it is ReviewRequest.Publication })
        assertEquals(2, store.state.pendingCount)
    }

    @Test
    fun `teacher sees only requests of own class`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(teacher.copy(pupilClass = "9-В"))

        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        assertEquals(listOf("publication-3"), store.state.queueRequests.map { it.id })
        assertEquals(0, store.state.pendingCount)
    }

    @Test
    fun `admin route shows visit queue`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(admin)

        store.accept(RequestsStore.Intent.Load(UserRole.ORGANIZATOR))
        runCurrent()

        assertTrue(store.state.canReview)
        assertEquals(RequestAudience.ADMIN, store.state.audience)
        assertTrue(store.state.visibleRequests.all { it is ReviewRequest.Visit })
        assertEquals(2, store.state.pendingCount)
    }

    @Test
    fun `teacher can open the admin queue through the route`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore(teacher)

            store.accept(RequestsStore.Intent.Load(UserRole.ORGANIZATOR))
            runCurrent()

            assertTrue(store.state.canReview)
            assertEquals(RequestAudience.ADMIN, store.state.audience)
            assertTrue(store.state.queueRequests.all { it is ReviewRequest.Visit })
            assertEquals(2, store.state.pendingCount)
        }

    @Test
    fun `pupil route has no reviewer access`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(pupil)

        store.accept(RequestsStore.Intent.Load(UserRole.PUPIL))
        runCurrent()

        assertFalse(store.state.canReview)
        assertNull(store.state.audience)
        assertTrue(store.state.visibleRequests.isEmpty())
    }

    @Test
    fun `pupil session cannot open a reviewer queue`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(pupil)

        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        assertFalse(store.state.canReview)
        assertEquals(RequestAudience.TEACHER, store.state.audience)
        assertTrue(store.state.visibleRequests.isEmpty())
    }

    @Test
    fun `approve publication changes status and publishes message`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore(teacher)
            store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
            runCurrent()

            store.labels.test {
                store.accept(RequestsStore.Intent.ClickApprove("publication-1"))
                runCurrent()
                store.accept(RequestsStore.Intent.ConfirmApprove)
                runCurrent()

                assertEquals(
                    RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_success_approved)),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }

            val updated = store.state.requests.filterIsInstance<ReviewRequest.Publication>()
                .first { it.id == "publication-1" }
            assertEquals(ReviewStatus.APPROVED, updated.status)
            assertNull(store.state.dialog)
        }

    @Test
    fun `approve visit stores confirmed details`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(admin)
        store.accept(RequestsStore.Intent.Load(UserRole.ORGANIZATOR))
        runCurrent()

        store.accept(RequestsStore.Intent.ClickApprove("visit-1"))
        runCurrent()

        val dialog = store.state.dialog as RequestDialog.Approve
        assertEquals("Суббота, 10:30", dialog.confirmedDate)

        store.accept(RequestsStore.Intent.ChangeConfirmedDate("Суббота, 11:00"))
        store.accept(RequestsStore.Intent.ChangeConfirmedLocation("Музей, главный вход"))
        store.accept(RequestsStore.Intent.ConfirmApprove)
        runCurrent()

        val updated = store.state.requests.filterIsInstance<ReviewRequest.Visit>()
            .first { it.id == "visit-1" }
        assertEquals(ReviewStatus.APPROVED, updated.status)
        assertEquals("Суббота, 11:00", updated.confirmedDate)
        assertEquals("Музей, главный вход", updated.confirmedLocation)
        assertNull(store.state.dialog)
    }

    @Test
    fun `approve visit without details keeps dialog open`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(admin)
        store.accept(RequestsStore.Intent.Load(UserRole.ORGANIZATOR))
        runCurrent()

        store.accept(RequestsStore.Intent.ClickApprove("visit-1"))
        runCurrent()
        store.accept(RequestsStore.Intent.ChangeConfirmedDate(""))
        store.accept(RequestsStore.Intent.ConfirmApprove)
        runCurrent()

        assertTrue(store.state.dialog is RequestDialog.Approve)
        val request = store.state.requests.filterIsInstance<ReviewRequest.Visit>()
            .first { it.id == "visit-1" }
        assertEquals(ReviewStatus.PENDING_ADMIN, request.status)
    }

    @Test
    fun `decline stores reason and closes dialog`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(teacher)
        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        store.accept(RequestsStore.Intent.ClickDecline("publication-1"))
        runCurrent()
        store.accept(RequestsStore.Intent.ChangeDeclineReason("Не хватает сопровождения"))
        store.accept(RequestsStore.Intent.ConfirmDecline)
        runCurrent()

        val updated = store.state.requests.filterIsInstance<ReviewRequest.Publication>()
            .first { it.id == "publication-1" }
        assertEquals(ReviewStatus.DECLINED, updated.status)
        assertEquals("Не хватает сопровождения", updated.declineReason)
        assertNull(store.state.dialog)
    }

    @Test
    fun `processed tab hides pending requests`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(teacher)
        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        store.accept(RequestsStore.Intent.SelectTab(RequestsTab.PROCESSED))
        runCurrent()

        assertTrue(store.state.visibleRequests.all { it.status == ReviewStatus.APPROVED || it.status == ReviewStatus.DECLINED })
    }
}
