package com.dev.lbsuhack.feature.feed.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.feed.data.FeedRepositoryImpl
import com.dev.lbsuhack.feature.feed.store.details.FeedDetailsStore
import com.dev.lbsuhack.feature.feed.store.details.FeedDetailsStoreFactory
import com.dev.lbsuhack.feature.feed.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedDetailsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): FeedDetailsStore = FeedDetailsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        feedRepository = FeedRepositoryImpl(),
    ).create()

    @Test
    fun `load finds event by id`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(FeedDetailsStore.Intent.Load("4"))
            runCurrent()

            assertFalse(store.state.isLoading)
            assertEquals("4", store.state.event?.id)
            assertFalse(store.state.isSubscribed)
        }

    @Test
    fun `unknown id produces not found error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(FeedDetailsStore.Intent.Load("missing"))
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNotNull(store.state.error)
            assertTrue(store.state.error is UiText.Resource)
        }

    @Test
    fun `toggle subscription subscribes and unsubscribes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(FeedDetailsStore.Intent.Load("4"))
            runCurrent()

            store.accept(FeedDetailsStore.Intent.ToggleSubscription)
            runCurrent()

            assertTrue(store.state.isSubscribed)
            assertFalse(store.state.isSubscribing)
            assertNotNull(store.state.event)

            store.accept(FeedDetailsStore.Intent.ToggleSubscription)
            runCurrent()

            assertFalse(store.state.isSubscribed)
            assertFalse(store.state.isSubscribing)
        }
}
