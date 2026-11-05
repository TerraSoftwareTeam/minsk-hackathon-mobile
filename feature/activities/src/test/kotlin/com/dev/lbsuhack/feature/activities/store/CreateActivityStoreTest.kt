package com.dev.lbsuhack.feature.activities.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
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
}
