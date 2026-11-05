package com.dev.lbsuhack.feature.feed.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.feature.feed.data.FeedRepositoryImpl
import com.dev.lbsuhack.feature.feed.domain.EventCategory
import com.dev.lbsuhack.feature.feed.store.feed.FeedStore
import com.dev.lbsuhack.feature.feed.store.feed.FeedStoreFactory
import com.dev.lbsuhack.feature.feed.util.MainDispatcherRule
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
class FeedStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): FeedStore = FeedStoreFactory(
        storeFactory = DefaultStoreFactory(),
        feedRepository = FeedRepositoryImpl(),
    ).create()

    @Test
    fun `load fills the state with events`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(FeedStore.Intent.Load)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals(7, store.state.events.size)
            assertEquals("Хакатон «Цифровой прорыв»", store.state.events.first().title)
            assertEquals(setOf("2", "6"), store.state.subscribedEvents.map { it.id }.toSet())
        }

    @Test
    fun `visible events exclude subscribed ones`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(FeedStore.Intent.Load)
            runCurrent()

            assertEquals(5, store.state.visibleEvents.size)
            assertTrue(store.state.visibleEvents.none { it.isSubscribed })
        }

    @Test
    fun `select category filters visible events`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(FeedStore.Intent.Load)
            runCurrent()

            store.accept(FeedStore.Intent.SelectCategory(EventCategory.HACKATHON))
            runCurrent()

            assertEquals(EventCategory.HACKATHON, store.state.selectedCategory)
            assertTrue(store.state.visibleEvents.all { it.category == EventCategory.HACKATHON })
            assertTrue(store.state.visibleEvents.isNotEmpty())

            store.accept(FeedStore.Intent.SelectCategory(null))
            runCurrent()

            assertEquals(store.state.events.filterNot { it.isSubscribed }, store.state.visibleEvents)
        }

    @Test
    fun `click event publishes open details label`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.labels.test {
                store.accept(FeedStore.Intent.ClickEvent("42"))

                assertEquals(FeedStore.Label.OpenDetails("42"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
