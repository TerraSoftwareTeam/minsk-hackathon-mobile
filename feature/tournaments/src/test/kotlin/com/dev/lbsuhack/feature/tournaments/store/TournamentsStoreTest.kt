package com.dev.lbsuhack.feature.tournaments.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.feature.tournaments.data.TournamentRepositoryImpl
import com.dev.lbsuhack.feature.tournaments.domain.TournamentStage
import com.dev.lbsuhack.feature.tournaments.store.tournaments.TournamentsStore
import com.dev.lbsuhack.feature.tournaments.store.tournaments.TournamentsStoreFactory
import com.dev.lbsuhack.feature.tournaments.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun `initial state defaults to school stage and no bracket`() {
        val store = createStore()

        assertEquals(TournamentStage.SCHOOL, store.state.stage)
        assertNull(store.state.bracket)
        assertNull(store.state.selectedMatch)
        assertFalse(store.state.isLoading)
        assertNull(store.state.error)
    }

    @Test
    fun `load fills the state with school stage bracket`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(TournamentsStore.Intent.Load)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            val bracket = store.state.bracket
            assertNotNull(bracket)
            assertEquals(TournamentStage.SCHOOL, bracket?.stage)
            assertEquals(7, bracket?.teamsCount)
            assertEquals(3, bracket?.rounds?.size)
        }

    @Test
    fun `select stage switches to district and loads 5 teams bracket`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(TournamentsStore.Intent.SelectStage(TournamentStage.DISTRICT))
            runCurrent()

            assertEquals(TournamentStage.DISTRICT, store.state.stage)
            val bracket = store.state.bracket
            assertNotNull(bracket)
            assertEquals(TournamentStage.DISTRICT, bracket?.stage)
            assertEquals(5, bracket?.teamsCount)
        }

    @Test
    fun `select match finds and sets selected match`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(TournamentsStore.Intent.Load)
            runCurrent()

            store.accept(TournamentsStore.Intent.SelectMatch("school-r0-m0"))
            assertNotNull(store.state.selectedMatch)
            assertEquals("school-r0-m0", store.state.selectedMatch?.id)

            store.accept(TournamentsStore.Intent.SelectMatch(null))
            assertNull(store.state.selectedMatch)
        }

    @Test
    fun `reset viewport increments trigger`() {
        val store = createStore()

        val initial = store.state.resetViewportTrigger
        store.accept(TournamentsStore.Intent.ResetViewport)
        assertEquals(initial + 1L, store.state.resetViewportTrigger)
    }
}
