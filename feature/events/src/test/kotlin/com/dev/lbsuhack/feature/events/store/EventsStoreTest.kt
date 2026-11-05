package com.dev.lbsuhack.feature.events.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.feature.events.data.EventRepositoryImpl
import com.dev.lbsuhack.feature.events.store.events.EventsStore
import com.dev.lbsuhack.feature.events.store.events.EventsStoreFactory
import com.dev.lbsuhack.feature.events.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): EventsStore = EventsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        eventRepository = EventRepositoryImpl(),
    ).create()

    @Test
    fun `load fills the state with events`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(EventsStore.Intent.Load)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals(3, store.state.events.size)
            assertEquals("Open Source Day", store.state.events.first().title)
        }

    @Test
    fun `click event publishes open details label`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.labels.test {
                store.accept(EventsStore.Intent.ClickEvent("3"))

                assertEquals(EventsStore.Label.OpenDetails("3"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
