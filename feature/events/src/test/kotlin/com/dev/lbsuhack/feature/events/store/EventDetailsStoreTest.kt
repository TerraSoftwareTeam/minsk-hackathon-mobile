package com.dev.lbsuhack.feature.events.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.events.data.EventRepositoryImpl
import com.dev.lbsuhack.feature.events.store.details.EventDetailsStore
import com.dev.lbsuhack.feature.events.store.details.EventDetailsStoreFactory
import com.dev.lbsuhack.feature.events.util.MainDispatcherRule
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
class EventDetailsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): EventDetailsStore = EventDetailsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        eventRepository = EventRepositoryImpl(),
    ).create()

    @Test
    fun `load finds event by id`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(EventDetailsStore.Intent.Load("1"))
            runCurrent()

            assertFalse(store.state.isLoading)
            assertEquals("1", store.state.event?.id)
        }

    @Test
    fun `unknown id produces not found error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(EventDetailsStore.Intent.Load("missing"))
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNotNull(store.state.error)
            assertTrue(store.state.error is UiText.Resource)
        }
}
