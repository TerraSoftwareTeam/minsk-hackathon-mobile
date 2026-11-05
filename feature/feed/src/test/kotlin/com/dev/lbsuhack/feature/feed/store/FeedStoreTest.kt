package com.dev.lbsuhack.feature.feed.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.feature.feed.data.FeedRepositoryImpl
import com.dev.lbsuhack.feature.feed.store.feed.FeedStore
import com.dev.lbsuhack.feature.feed.store.feed.FeedStoreFactory
import com.dev.lbsuhack.feature.feed.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
    fun `load fills the state with posts`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(FeedStore.Intent.Load)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals(5, store.state.posts.size)
            assertEquals("Welcome to the hackathon", store.state.posts.first().title)
        }

    @Test
    fun `click post publishes open details label`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.labels.test {
                store.accept(FeedStore.Intent.ClickPost("42"))

                assertEquals(FeedStore.Label.OpenDetails("42"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
