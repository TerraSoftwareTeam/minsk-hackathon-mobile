package com.dev.lbsuhack.feature.profile.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.feature.profile.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): ProfileStore =
        ProfileStoreFactory(storeFactory = DefaultStoreFactory()).create()

    @Test
    fun `initial state has expected default profile values`() {
        val store = createStore()

        assertEquals("HackathonDev", store.state.username)
        assertEquals("dev@hackathon.org", store.state.email)
        assertTrue(store.state.notificationsEnabled)
        assertEquals(12, store.state.savedItemsCount)
    }

    @Test
    fun `when SetUsername intent is dispatched then state username is updated`() {
        val store = createStore()

        store.accept(ProfileStore.Intent.SetUsername("AndroidChampion"))

        assertEquals("AndroidChampion", store.state.username)
    }

    @Test
    fun `when ToggleNotifications intent is dispatched then status is toggled and label is emitted`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()
        assertTrue(store.state.notificationsEnabled)

        store.labels.test {
            store.accept(ProfileStore.Intent.ToggleNotifications)

            assertFalse(store.state.notificationsEnabled)
            val label = awaitItem()
            assertTrue(label is ProfileStore.Label.NotificationToggled)
            assertFalse((label as ProfileStore.Label.NotificationToggled).enabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when IncrementSaved intent is dispatched then saved count increases`() {
        val store = createStore()
        val initialCount = store.state.savedItemsCount

        store.accept(ProfileStore.Intent.IncrementSaved)
        assertEquals(initialCount + 1, store.state.savedItemsCount)

        store.accept(ProfileStore.Intent.IncrementSaved)
        assertEquals(initialCount + 2, store.state.savedItemsCount)
    }
}
