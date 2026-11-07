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
import com.dev.lbsuhack.feature.requests.domain.MAX_DECLINE_REASON_LENGTH
import com.dev.lbsuhack.feature.requests.domain.RequestAudience
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import com.dev.lbsuhack.feature.requests.domain.isProcessed
import com.dev.lbsuhack.feature.requests.store.requests.AccessState
import com.dev.lbsuhack.feature.requests.store.requests.RequestDialog
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStore
import com.dev.lbsuhack.feature.requests.store.requests.RequestsStoreFactory
import com.dev.lbsuhack.feature.requests.store.requests.RequestsTab
import com.dev.lbsuhack.feature.requests.util.FakeReviewRequestRepository
import com.dev.lbsuhack.feature.requests.util.FakeSessionRepository
import com.dev.lbsuhack.feature.requests.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.OffsetDateTime

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
        status: ReviewStatus = ReviewStatus.PENDING_TEACHER,
        authorName: String = "Ученик $id",
    ) = ReviewRequest(
        id = id,
        title = "Заявка $id",
        description = "Описание",
        proposedDate = OffsetDateTime.parse("2026-11-21T10:00:00+03:00"),
        authorName = authorName,
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
        proposedDate = OffsetDateTime.parse("2026-11-21T10:00:00+03:00"),
        authorName = "Ученик $id",
        status = status,
        audience = RequestAudience.ADMIN,
    )

    private fun createStore(
        sessionRepository: FakeSessionRepository,
        repository: FakeReviewRequestRepository,
    ): RequestsStore = RequestsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        requestRepository = repository,
        observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
    ).create()

    private fun createStore(
        user: SessionUser,
        repository: FakeReviewRequestRepository,
    ): RequestsStore = createStore(
        sessionRepository = FakeSessionRepository(initialState = SessionState.Authenticated(user)),
        repository = repository,
    )

    @Test
    fun `polling picks up a request that arrives while the screen is open`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeReviewRequestRepository(teacherQueue = listOf(teacherRequest("existing")))
            val store = RequestsStoreFactory(
                storeFactory = DefaultStoreFactory(),
                requestRepository = repository,
                observeSessionUseCase = ObserveSessionUseCase(
                    FakeSessionRepository(initialState = SessionState.Authenticated(teacher)),
                ),
                pollIntervalMs = POLL_INTERVAL_MS,
            ).create()

            store.labels.test {
                store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
                store.accept(RequestsStore.Intent.StartPolling)
                runCurrent()
                assertEquals(listOf("existing"), store.state.queueRequests.map { it.id })

                repository.teacherQueue = listOf(teacherRequest("existing"), teacherRequest("fresh"))
                advanceTimeBy(POLL_INTERVAL_MS + 1)
                runCurrent()

                assertEquals(listOf("existing", "fresh"), store.state.queueRequests.map { it.id })
                val label = awaitItem() as RequestsStore.Label.ShowMessage
                assertEquals(UiText.Plural(R.plurals.requests_new_arrived, 1), label.text)

                store.accept(RequestsStore.Intent.StopPolling)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }

    @Test
    fun `admin approve dialog shows the author date and chance`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val date = OffsetDateTime.now().plusDays(10).withNano(0)
            val repository = FakeReviewRequestRepository(
                adminQueue = listOf(adminRequest("a-1").copy(proposedDate = date, successProbability = 0.58)),
            )
            val store = createStore(user = admin, repository = repository)
            store.accept(RequestsStore.Intent.Load(UserRole.ORGANIZATOR))
            runCurrent()

            store.accept(RequestsStore.Intent.ClickApprove("a-1"))
            val opened = store.state.dialog as RequestDialog.Approve
            assertEquals(date, opened.startsAt)
            assertEquals(0.58, opened.chance!!, 0.0001)
            store.dispose()
        }

    @Test
    fun `teacher queue shows requests without class filtering`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(
                    teacherRequest("publication-1", authorName = "Ученик без класса"),
                    teacherRequest("publication-2"),
                    teacherRequest("publication-3", ReviewStatus.APPROVED),
                ),
            ),
        )

        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        assertEquals(AccessState.Granted, store.state.accessState)
        assertTrue(store.state.canReview)
        assertEquals(RequestAudience.TEACHER, store.state.audience)
        assertEquals(teacher.name, store.state.userName)
        assertEquals(
            listOf("publication-1", "publication-2", "publication-3"),
            store.state.queueRequests.map { it.id },
        )
        assertEquals(2, store.state.pendingCount)
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

        assertEquals(AccessState.Granted, store.state.accessState)
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

            assertEquals(AccessState.Denied, store.state.accessState)
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
                teacherQueue = listOf(teacherRequest("publication-1")),
            ),
        )

        store.accept(RequestsStore.Intent.Load(UserRole.PUPIL))
        runCurrent()

        assertEquals(AccessState.Denied, store.state.accessState)
        assertFalse(store.state.canReview)
        assertNull(store.state.audience)
        assertTrue(store.state.visibleRequests.isEmpty())
    }

    @Test
    fun `access stays checking while the session is unknown`() = runTest(mainDispatcherRule.testDispatcher) {
        val sessionRepository = FakeSessionRepository(initialState = SessionState.Unknown)
        val store = createStore(
            sessionRepository = sessionRepository,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(teacherRequest("publication-1")),
            ),
        )

        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        assertEquals(AccessState.Checking, store.state.accessState)
        assertFalse(store.state.canReview)
        assertTrue(store.state.queueRequests.isEmpty())

        sessionRepository.authenticate(teacher)
        runCurrent()

        assertEquals(AccessState.Granted, store.state.accessState)
        assertEquals(RequestAudience.TEACHER, store.state.audience)
        assertEquals(listOf("publication-1"), store.state.queueRequests.map { it.id })
    }

    @Test
    fun `empty cache emission does not finish the initial loading`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore(
                user = teacher,
                repository = FakeReviewRequestRepository(
                    teacherQueue = listOf(teacherRequest("publication-1")),
                    refreshDelayMillis = 1_000L,
                ),
            )

            store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
            runCurrent()

            assertTrue(store.state.requests.isEmpty())
            assertTrue(store.state.isRefreshing)
            assertTrue(store.state.isInitialLoading)

            advanceUntilIdle()

            assertEquals(listOf("publication-1"), store.state.requests.map { it.id })
            assertFalse(store.state.isRefreshing)
            assertFalse(store.state.isInitialLoading)
        }

    @Test
    fun `refresh failure marks the load as failed and retry succeeds`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeReviewRequestRepository(
                teacherQueue = listOf(teacherRequest("publication-1")),
                refreshResult = DomainResult.Failure(DomainError.Network),
            )
            val store = createStore(user = teacher, repository = repository)

            store.labels.test {
                store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
                runCurrent()

                assertEquals(
                    RequestsStore.Label.ShowMessage(UiText.Resource(R.string.requests_error_network)),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }

            assertTrue(store.state.loadFailed)
            assertFalse(store.state.isInitialLoading)
            assertTrue(store.state.requests.isEmpty())

            repository.refreshResult = DomainResult.Success(Unit)
            store.accept(RequestsStore.Intent.Refresh)
            advanceUntilIdle()

            assertFalse(store.state.loadFailed)
            assertEquals(listOf("publication-1"), store.state.requests.map { it.id })
        }

    @Test
    fun `approve changes the status and publishes a message`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore(
                user = teacher,
                repository = FakeReviewRequestRepository(
                    teacherQueue = listOf(teacherRequest("publication-1")),
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
            assertFalse(store.state.isSubmitting)
        }

    @Test
    fun `decline stores the reason and closes the dialog`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(teacherRequest("publication-1")),
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
    fun `decline reason is truncated to the maximum length`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(teacherRequest("publication-1")),
            ),
        )
        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        store.accept(RequestsStore.Intent.ClickDecline("publication-1"))
        runCurrent()
        store.accept(RequestsStore.Intent.ChangeDeclineReason("я".repeat(MAX_DECLINE_REASON_LENGTH + 500)))
        runCurrent()

        val dialog = store.state.dialog as RequestDialog.Decline
        assertEquals(MAX_DECLINE_REASON_LENGTH, dialog.reason.length)
    }

    @Test
    fun `dialog is not dismissed while the request is being submitted`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore(
                user = teacher,
                repository = FakeReviewRequestRepository(
                    teacherQueue = listOf(teacherRequest("publication-1")),
                    actionDelayMillis = 1_000L,
                ),
            )
            store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
            runCurrent()

            store.accept(RequestsStore.Intent.ClickDecline("publication-1"))
            runCurrent()
            store.accept(RequestsStore.Intent.ConfirmDecline)
            runCurrent()

            assertTrue(store.state.isSubmitting)
            assertNotNull(store.state.dialog)

            store.accept(RequestsStore.Intent.DismissDialog)
            runCurrent()

            assertNotNull(store.state.dialog)

            advanceUntilIdle()

            assertFalse(store.state.isSubmitting)
            assertNull(store.state.dialog)
        }

    @Test
    fun `admin approves without choosing a date`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = admin,
            repository = FakeReviewRequestRepository(
                adminQueue = listOf(adminRequest("admin-1")),
            ),
        )
        store.accept(RequestsStore.Intent.Load(UserRole.ORGANIZATOR))
        runCurrent()

        store.accept(RequestsStore.Intent.ClickApprove("admin-1"))
        runCurrent()

        store.accept(RequestsStore.Intent.ConfirmApprove)
        runCurrent()

        assertEquals(ReviewStatus.APPROVED, store.state.requests.first { it.id == "admin-1" }.status)
        assertNull(store.state.dialog)
    }

    @Test
    fun `processed tab hides pending requests`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore(
            user = teacher,
            repository = FakeReviewRequestRepository(
                teacherQueue = listOf(
                    teacherRequest("publication-1"),
                    teacherRequest("publication-2", ReviewStatus.DECLINED),
                ),
            ),
        )
        store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
        runCurrent()

        store.accept(RequestsStore.Intent.SelectTab(RequestsTab.PROCESSED))
        runCurrent()

        assertTrue(store.state.visibleRequests.all { it.isProcessed })
        assertEquals(listOf("publication-2"), store.state.visibleRequests.map { it.id })
    }

    @Test
    fun `pause stops the observation and resume reloads the queue`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore(
                user = teacher,
                repository = FakeReviewRequestRepository(
                    teacherQueue = listOf(teacherRequest("publication-1")),
                ),
            )
            store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
            runCurrent()

            assertEquals(listOf("publication-1"), store.state.requests.map { it.id })

            store.accept(RequestsStore.Intent.Pause)
            runCurrent()

            store.accept(RequestsStore.Intent.Load(UserRole.TEACHER))
            runCurrent()

            assertEquals(listOf("publication-1"), store.state.requests.map { it.id })
            assertEquals(AccessState.Granted, store.state.accessState)
        }
}

private const val POLL_INTERVAL_MS = 1_000L

