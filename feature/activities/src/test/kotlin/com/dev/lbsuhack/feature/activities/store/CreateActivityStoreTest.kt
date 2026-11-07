package com.dev.lbsuhack.feature.activities.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStore
import com.dev.lbsuhack.feature.activities.store.create.CreateActivityStoreFactory
import com.dev.lbsuhack.feature.activities.store.create.CreationState
import com.dev.lbsuhack.feature.activities.util.FakeActivityRepository
import com.dev.lbsuhack.feature.activities.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private fun CreateActivityStore.fillValidForm() {
        accept(CreateActivityStore.Intent.ChangeTitle("Турнир"))
        accept(CreateActivityStore.Intent.ChangeDescription("Описание"))
    }

    @Test
    fun emptyTitleIsRejected() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(CreateActivityStore.Intent.ChangeDescription("Описание"))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertNotNull(store.state.error)
        assertEquals(0, activityRepository.createCalls)
        store.dispose()
    }

    @Test
    fun emptyDescriptionIsRejected() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(CreateActivityStore.Intent.ChangeTitle("Турнир"))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertNotNull(store.state.error)
        assertEquals(0, activityRepository.createCalls)
        store.dispose()
    }

    @Test
    fun voteThresholdOutOfRangeIsRejected() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.fillValidForm()

        store.accept(CreateActivityStore.Intent.ChangeVoteThreshold(0))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertNotNull(store.state.error)
        assertEquals(0, activityRepository.createCalls)
        store.dispose()
    }

    @Test
    fun successfulSubmissionCreatesActivityAndLocksForm() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.fillValidForm()

        store.labels.test {
            store.accept(CreateActivityStore.Intent.Submit)
            runCurrent()

            assertEquals(
                CreateActivityStore.Label.Created(FakeActivityRepository.CREATED_ID),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(CreationState.CREATED, store.state.creationState)
        assertFalse(store.state.canEdit)
        assertEquals(1, activityRepository.createCalls)
        store.dispose()
    }

    @Test
    fun formStaysLockedAfterCreation() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        store.fillValidForm()

        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        store.accept(CreateActivityStore.Intent.ChangeTitle("Другое название"))
        runCurrent()

        assertEquals("Турнир", store.state.title)
        store.dispose()
    }

    @Test
    fun failedPhotoUploadSwitchesToPhotoPendingAndKeepsCreatedId() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.photoResult = DomainResult.Failure(DomainError.Network)
        val store = createStore()
        store.fillValidForm()
        store.accept(CreateActivityStore.Intent.ChangePhoto("content://photo"))

        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertEquals(CreationState.PHOTO_PENDING, store.state.creationState)
        assertTrue(store.state.canRetryPhoto)
        assertNotNull(store.state.createdActivityId)
        assertNotNull(store.state.error)
        store.dispose()
    }

    @Test
    fun retryAfterPhotoFailureUploadsPhotoOnly() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.photoResult = DomainResult.Failure(DomainError.Network)
        val store = createStore()
        store.fillValidForm()
        store.accept(CreateActivityStore.Intent.ChangePhoto("content://photo"))

        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()
        assertEquals(1, activityRepository.createCalls)

        activityRepository.photoResult = null
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertEquals(1, activityRepository.createCalls)
        assertEquals(2, activityRepository.photoCalls)
        assertEquals(CreationState.CREATED, store.state.creationState)
        store.dispose()
    }

    @Test
    fun droppingPhotoFinishesCreation() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.photoResult = DomainResult.Failure(DomainError.Network)
        val store = createStore()
        store.fillValidForm()
        store.accept(CreateActivityStore.Intent.ChangePhoto("content://photo"))

        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        store.accept(CreateActivityStore.Intent.ChangePhoto(null))
        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertEquals(CreationState.CREATED, store.state.creationState)
        assertNull(store.state.photoUri)
        store.dispose()
    }

    @Test
    fun failedCreationKeepsFormEditable() = runTest(mainDispatcherRule.testDispatcher) {
        activityRepository.createResult = DomainResult.Failure(DomainError.Forbidden)
        val store = createStore()
        store.fillValidForm()

        store.accept(CreateActivityStore.Intent.Submit)
        runCurrent()

        assertEquals(CreationState.EDITING, store.state.creationState)
        assertTrue(store.state.canEdit)
        assertNotNull(store.state.error)
        store.dispose()
    }
}
