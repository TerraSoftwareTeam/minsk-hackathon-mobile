package com.dev.lbsuhack.feature.requests.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.requests.R
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.RequestAuthor
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.domain.isProcessed
import com.dev.lbsuhack.feature.requests.store.requests.RequestDialog
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStoreFactory
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab
import com.dev.lbsuhack.feature.requests.util.FakeReviewRequestRepository
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

    private fun teacherRequest(
        id: String,
        pupilClass: String,
        status: ReviewStatus = ReviewStatus.PENDING_TEACHER,
    ) = ReviewRequest(
        id = id,
        title = "Заявка $id",
        description = "Описание",
        proposedDate = "Пятница, 16:00",
        author = RequestAuthor("pupil-$id", "Ученик $id", pupilClass),
        status = status,
        audience = RequestAudience.TEACHER,
    )

    private fun adminRequest(
        id: String,
        status: ReviewStatus = ReviewStatus.PENDING_ADMIN,
    ) = ReviewRequest(
        id = id,
        title = "Заявка $id",
        description = "Описание",
        proposedDate = "Суббота, 10:30",
        author = RequestAuthor("pupil-$id", "Ученик $id", "11-А"),
        status = status,
        audience = RequestAudience.ADMIN,
    )

    private fun createStore(
        user: SessionUser,
        repository: FakeReviewRequestRepository,
    ): RequestsStore {
        val sessionRepository = FakeSessionRepository(
            initialState = SessionState.Authenticated(user),
        )
        return RequestsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            requestRepository = repository,
            observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
        ).create()
    }

    @Test
    fun `teacher route shows publication queue`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(
                    teacherRequest("publication-1", "10-А"),
                    teacherRequest("publication-2", "10-А"),
                    teacherRequest("publication-3", "10-А", ReviewStatus.APPROVED),
                ),
            ),
        )

        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        assertTrue(store.state.canReview)
        assertEquals(RequestAudience.TEACHER, store.state.audience)
        assertTrue(store.state.queueRequests.all { it.audience == RequestAudience.TEACHER })
        assertEquals(2, store.state.pendingCount)
    }

    @Test
    fun `teacher sees only requests of own class`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher.copy(pupilClass = "9-В"),
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(
                    teacherRequest("publication-1", "10-А"),
                    teacherRequest("publication-3", "9-В", ReviewStatus.APPROVED),
                ),
            ),
        )

        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        assertEquals(listOf("publication-3"), store.state.queueRequests.map { it.id })
        assertEquals(0, store.state.pendingCount)
    }

    @Test
    fun `admin route shows admin queue`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = admin,
            repository = FakeReviewRequestRepository(
                adminQueue = listOf(
                    adminRequest("admin-1"),
                    adminRequest("admin-2"),
                    adminRequest("admin-3", ReviewStatus.APPROVED),
                ),
            ),
        )

        store.accept(RequestsStore.Intent.Load(UserRole.ORGANIZATOR))
        runCurrent()

        assertTrue(store.state.canReview)
        assertEquals(RequestAudience.ADMIN, store.state.audience)
        assertTrue(store.state.queueRequests.all { it.audience == RequestAudience.ADMIN })
        assertEquals(2, store.state.pendingCount)
    }

    @Test
    fun `teacher cannot open the admin queue through the route`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore(
                user = teacher,
                repository = FakeReviewRequestRepository(
                    adminQueue = listOf(adminRequest("admin-1")),
                ),
            )

            store.accept(RequestsStore.Intent.Load(UserRole.ORGANIZATOR))
            runCurrent()

            assertFalse(store.state.canReview)
            assertNull(store.state.audience)
            assertTrue(store.state.queueRequests.isEmpty())
            assertEquals(0, store.state.pendingCount)
        }

    @Test
    fun `pupil route has no reviewer access`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = pupil,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(teacherRequest("publication-1", "10-А")),
            ),
        )

        store.accept(RequestsStore.Intent.Load(UserRole.PUPIL))
        runCurrent()

        assertFalse(store.state.canReview)
        assertNull(store.state.audience)
        assertTrue(store.state.visibleRequests.isEmpty())
    }

    @Test
    fun `pupil session cannot open a reviewer queue`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = pupil,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(teacherRequest("publication-1", "10-А")),
            ),
        )

        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        assertFalse(store.state.canReview)
        assertNull(store.state.audience)
        assertTrue(store.state.visibleRequests.isEmpty())
    }

    @Test
    fun `approve publication changes status and publishes message`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore(
                user = teacher,
                repository = FakeReviewRequestRepository(
                    teacherQueue = listOf(teacherRequest("publication-1", "10-А")),
                ),
            )
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

            val updated = store.state.requests.first { it.id == "publication-1" }
            assertEquals(ReviewStatus.APPROVED, updated.status)
            assertNull(store.state.dialog)
        }

    @Test
    fun `decline stores reason and closes dialog`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(teacherRequest("publication-1", "10-А")),
            ),
        )
        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        store.accept(RequestsStore.Intent.ClickDecline("publication-1"))
        runCurrent()
        store.accept(RequestsStore.Intent.ChangeDeclineReason("Не хватает сопровождения"))
        store.accept(RequestsStore.Intent.ConfirmDecline)
        runCurrent()

        val updated = store.state.requests.first { it.id == "publication-1" }
        assertEquals(ReviewStatus.DECLINED, updated.status)
        assertEquals("Не хватает сопровождения", updated.declineReason)
        assertNull(store.state.dialog)
    }

    @Test
    fun `processed tab hides pending requests`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(
                    teacherRequest("publication-1", "10-А"),
                    teacherRequest("publication-2", "10-А", ReviewStatus.DECLINED),
                ),
            ),
        )
        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        store.accept(RequestsStore.Intent.SelectTab(RequestsTab.PROCESSED))
        runCurrent()

        assertTrue(store.state.visibleRequests.all { it.isProcessed })
    }

    @Test
    fun `refresh failure publishes error and stops loading`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(teacherRequest("publication-1", "10-А")),
                refreshResult = DomainResult.Failure(DomainError.Network),
            ),
        )

        store.labels.test {
            store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
            runCurrent()

            assertEquals(
                RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_error_network)),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(store.state.isLoading)
        assertTrue(store.state.visibleRequests.isEmpty())
    }
}
