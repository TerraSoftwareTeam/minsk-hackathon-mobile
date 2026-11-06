package com.dev.lbsuhack.feature.activities.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.activities.domain.ActivityInitiative
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.data.ActivityRepositoryImpl
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStore
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStoreFactory
import com.dev.lbsuhack.feature.activities.util.FakeSessionRepository
import com.dev.lbsuhack.feature.activities.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateActivityStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionUser = SessionUser(
        id = "101",
        name = "Никита Ковалев",
        schoolName = "School #1",
        pupilClass = "10-А",
    )

    private val sessionRepository = FakeSessionRepository(
        initialState = SessionState.Authenticated(sessionUser),
    )

    private val activityRepository = ActivityRepositoryImpl()

    private fun createStore(): CreateActivityStore = CreateActivityStoreFactory(
        storeFactory = DefaultStoreFactory(),
        activityRepository = activityRepository,
        observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun submitWithEmptyTitleShowsError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertNotNull(store.state.error)
    }

    @Test
    fun submitWithEmptyDescriptionShowsError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(CreateActivityStore.Intent.ChangeTitle("Клуб дебатов"))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertNotNull(store.state.error)
    }

    @Test
    fun submitWithInvalidLimitsShowsError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(CreateActivityStore.Intent.ChangeTitle("Клуб дебатов"))
        store.accept(CreateActivityStore.Intent.ChangeDescription("Обсуждаем темы"))
        store.accept(CreateActivityStore.Intent.ChangeMinParticipants(10))
        store.accept(CreateActivityStore.Intent.ChangeMaxParticipants(4))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertNotNull(store.state.error)
    }

    @Test
    fun submitValidDataCreatesProposalAndPublishesLabel() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.labels.test {
            store.accept(CreateActivityStore.Intent.ChangeCategory(ActivityCategory.SCIENCE))
            store.accept(CreateActivityStore.Intent.ChangeTitle("Клуб робототехники"))
            store.accept(CreateActivityStore.Intent.ChangeDescription("Собираем роботов"))
            store.accept(CreateActivityStore.Intent.ChangeMinParticipants(3))
            store.accept(CreateActivityStore.Intent.ChangeMaxParticipants(8))
            store.accept(CreateActivityStore.Intent.Submit)

            val label = awaitItem()
            assertTrue(label is CreateActivityStore.Label.Created)
            assertNull(store.state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun selectedPhotoIsIncludedInTheCreatedActivity() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        val photo = "content://media/external/images/media/42"

        store.labels.test {
            store.accept(CreateActivityStore.Intent.ChangeTitle("Фотопрогулка"))
            store.accept(CreateActivityStore.Intent.ChangeDescription("Снимаем город"))
            store.accept(CreateActivityStore.Intent.ChangePhoto(photo))
            store.accept(CreateActivityStore.Intent.Submit)

            val label = awaitItem() as CreateActivityStore.Label.Created
            assertEquals(photo, activityRepository.getActivityById(label.activityId)?.photoUri)
            assertNull(store.state.error)
            cancelAndIgnoreRemainingEvents()
        }
        store.dispose()
    }

    @Test
    fun activityCanBeCreatedWithoutAPhoto() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.labels.test {
            store.accept(CreateActivityStore.Intent.ChangeTitle("Встреча"))
            store.accept(CreateActivityStore.Intent.ChangeDescription("Обсуждаем планы"))
            store.accept(CreateActivityStore.Intent.Submit)

            val label = awaitItem() as CreateActivityStore.Label.Created
            assertNotNull(activityRepository.getActivityById(label.activityId))
            assertNull(activityRepository.getActivityById(label.activityId)?.photoUri)
            assertNull(store.state.error)
            cancelAndIgnoreRemainingEvents()
        }
        store.dispose()
    }

    @Test
    fun photoCanBeReplacedAndRemovedBeforeSubmitting() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(CreateActivityStore.Intent.ChangePhoto("content://photos/first"))
        store.accept(CreateActivityStore.Intent.ChangePhoto("content://photos/second"))
        assertEquals("content://photos/second", store.state.photoUri)
        store.accept(CreateActivityStore.Intent.ChangePhoto(null))
        assertNull(store.state.photoUri)

        store.labels.test {
            store.accept(CreateActivityStore.Intent.ChangeTitle("Встреча"))
            store.accept(CreateActivityStore.Intent.ChangeDescription("Обсуждаем планы"))
            store.accept(CreateActivityStore.Intent.Submit)

            val label = awaitItem() as CreateActivityStore.Label.Created
            assertNull(activityRepository.getActivityById(label.activityId)?.photoUri)
            cancelAndIgnoreRemainingEvents()
        }
        store.dispose()
    }

    @Test
    fun initiativeIsRecordedFromTheAuthorRole() = runTest(mainDispatcherRule.testDispatcher) {
        val roles = mapOf(
            UserRole.PUPIL to ActivityInitiative.STUDENT,
            UserRole.TEACHER to ActivityInitiative.SCHOOL,
            UserRole.ORGANIZATOR to ActivityInitiative.SCHOOL,
        )
        roles.forEach { (role, expected) ->
            sessionRepository.save(sessionUser.copy(role = role))
            val store = createStore()
            store.labels.test {
                store.accept(CreateActivityStore.Intent.ChangeTitle("Мероприятие"))
                store.accept(CreateActivityStore.Intent.ChangeDescription("Описание"))
                store.accept(CreateActivityStore.Intent.Submit)
                val created = awaitItem() as CreateActivityStore.Label.Created
                val proposal = activityRepository.getActivityById(created.activityId)!!
                assertEquals(expected, proposal.initiative)
                val approved = activityRepository.approveActivity(proposal.id, "12:00", "Школа")
                assertEquals(expected, approved.initiative)
                cancelAndIgnoreRemainingEvents()
            }
            store.dispose()
        }
    }

}
