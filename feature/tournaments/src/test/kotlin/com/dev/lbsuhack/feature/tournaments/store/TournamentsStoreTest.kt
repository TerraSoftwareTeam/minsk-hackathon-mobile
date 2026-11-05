package com.dev.lbsuhack.feature.tournaments.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.feature.tournaments.data.TournamentRepositoryImpl
import com.dev.lbsuhack.feature.tournaments.store.tournaments.TournamentsStore
import com.dev.lbsuhack.feature.tournaments.store.tournaments.TournamentsStoreFactory
import com.dev.lbsuhack.feature.tournaments.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TournamentsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): TournamentsStore = TournamentsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        tournamentRepository = TournamentRepositoryImpl(),
    ).create()

    @Test
    fun `load fills the state with tournaments`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(TournamentsStore.Intent.Load)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals(3, store.state.tournaments.size)
            assertEquals("Spring Mobile Cup", store.state.tournaments.first().title)
        }

    @Test
    fun `click tournament publishes open details label`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.labels.test {
                store.accept(TournamentsStore.Intent.ClickTournament("2"))

                assertEquals(TournamentsStore.Label.OpenDetails("2"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
