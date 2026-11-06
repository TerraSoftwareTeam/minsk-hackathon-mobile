package com.dev.lbsuhack.feature.activities.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStore
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStoreFactory
import com.dev.lbsuhack.feature.activities.util.FakeActivityRepository
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

    private val activityRepository = FakeActivityRepository()

    private fun createStore(): CreateActivityStore = CreateActivityStoreFactory(
        storeFactory = DefaultStoreFactory(),
        activityRepository = activityRepository,
    ).create()

    @Test
    fun submitWithEmptyTitleShowsError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertNotNull(store.state.error)
        store.dispose()
    }

    @Test
    fun submitWithEmptyDescriptionShowsError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(CreateActivityStore.Intent.ChangeTitle("Клуб дебатов"))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertNotNull(store.state.error)
        store.dispose()
    }

    @Test
    fun submitValidDataCreatesProposalAndPublishesLabel() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.labels.test {
            store.accept(CreateActivityStore.Intent.ChangeCategory(ActivityCategory.SPORTS))
            store.accept(CreateActivityStore.Intent.ChangeTitle("Клуб робототехники"))
            store.accept(CreateActivityStore.Intent.ChangeDescription("Собираем роботов"))
            store.accept(CreateActivityStore.Intent.ChangeScope(ActivityScope.SCHOOL))
            store.accept(CreateActivityStore.Intent.ChangeVoteThreshold(15))
            store.accept(CreateActivityStore.Intent.Submit)

            val label = awaitItem()
            assertTrue(label is CreateActivityStore.Label.Created)
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
        store.dispose()
    }

    @Test
    fun submitFailureShowsDomainError() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        activityRepository.createResult = DomainResult.Failure(
            DomainError.BusinessRule(message = "Pupil class has no teacher"),
        )

        store.accept(CreateActivityStore.Intent.ChangeCategory(ActivityCategory.SPORTS))
        store.accept(CreateActivityStore.Intent.ChangeTitle("Клуб робототехники"))
        store.accept(CreateActivityStore.Intent.ChangeDescription("Собираем роботов"))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertEquals(UiText.Raw("Pupil class has no teacher"), store.state.error)
        store.dispose()
    }
    @Test
    fun photoRetryDoesNotCreateDuplicateEvent() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        activityRepository.photoResult = DomainResult.Failure(DomainError.Network)
        store.accept(CreateActivityStore.Intent.ChangeTitle("Клуб"))
        store.accept(CreateActivityStore.Intent.ChangeDescription("Описание"))
        store.accept(CreateActivityStore.Intent.ChangePhoto("content://photos/first"))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()
        assertEquals("created-id", store.state.createdActivityId)
        assertNotNull(store.state.error)
        activityRepository.photoResult = null
        store.labels.test {
            store.accept(CreateActivityStore.Intent.Submit)
            assertTrue(awaitItem() is CreateActivityStore.Label.Created)
        }
        assertEquals(1, activityRepository.createCalls)
        assertEquals(2, activityRepository.photoCalls)
        store.dispose()
    }

    @Test
    fun voteThresholdOutsideLimitsDoesNotReachBackend() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.accept(CreateActivityStore.Intent.ChangeTitle("Клуб"))
        store.accept(CreateActivityStore.Intent.ChangeDescription("Описание"))
        store.accept(CreateActivityStore.Intent.ChangeVoteThreshold(10001))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()
        assertNotNull(store.state.error)
        assertEquals(0, activityRepository.createCalls)
        store.dispose()
    }
}
