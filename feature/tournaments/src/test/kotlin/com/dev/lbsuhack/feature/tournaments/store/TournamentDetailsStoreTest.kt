package com.dev.lbsuhack.feature.tournaments.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.tournaments.data.TournamentRepositoryImpl
import com.dev.lbsuhack.feature.tournaments.store.details.TournamentDetailsStore
import com.dev.lbsuhack.feature.tournaments.store.details.TournamentDetailsStoreFactory
import com.dev.lbsuhack.feature.tournaments.util.MainDispatcherRule
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
class TournamentDetailsStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createStore(): TournamentDetailsStore = TournamentDetailsStoreFactory(
        storeFactory = DefaultStoreFactory(),
        tournamentRepository = TournamentRepositoryImpl(),
    ).create()

    @Test
    fun `load finds tournament by id`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(TournamentDetailsStore.Intent.Load("2"))
            runCurrent()

            assertFalse(store.state.isLoading)
            assertEquals("2", store.state.tournament?.id)
        }

    @Test
    fun `unknown id produces not found error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(TournamentDetailsStore.Intent.Load("missing"))
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNotNull(store.state.error)
            assertTrue(store.state.error is UiText.Resource)
        }
}
