package com.dev.lbsuhack.feature.home.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.localization.UiText
import com.dev.lbsuhack.feature.home.R
import com.dev.lbsuhack.feature.home.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): HomeStore =
        HomeStoreFactory(storeFactory = DefaultStoreFactory()).create()

    @Test
    fun `initial state has default items and is not loading`() {
        val store = createStore()

        assertFalse(store.state.isLoading)
        assertEquals(3, store.state.items.size)
        assertEquals("1", store.state.items.first().id)
        assertEquals(
            R.string.home_item_1_title,
            (store.state.items.first().title as UiText.Resource).id,
        )
    }

    @Test
    fun `when ClickItem intent is dispatched then OpenDetails label is published`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.labels.test {
            store.accept(HomeStore.Intent.ClickItem(id = "42"))

            val label = awaitItem()
            assertTrue(label is HomeStore.Label.OpenDetails)
            val details = label as HomeStore.Label.OpenDetails
            assertEquals("42", details.itemId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when Refresh intent is dispatched then items are updated after delay`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(HomeStore.Intent.Refresh)
        runCurrent()
        // Check that loading started
        assertTrue(store.state.isLoading)

        // Advance past delay(600)
        advanceTimeBy(700)
        runCurrent()

        // Loading finished and 4 updated items loaded
        assertFalse(store.state.isLoading)
        assertEquals(4, store.state.items.size)
        assertEquals(
            R.string.home_item_1_title_refreshed,
            (store.state.items.first().title as UiText.Resource).id,
        )
    }
}
