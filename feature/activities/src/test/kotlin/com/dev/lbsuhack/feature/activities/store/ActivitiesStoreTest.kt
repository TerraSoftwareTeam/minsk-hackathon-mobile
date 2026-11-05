package com.dev.lbsuhack.feature.activities.store

import app.cash.turbine.test
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.activities.data.ActivityRepositoryImpl
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStore
import com.dev.lbsuhack.feature.activities.store.activities.ActivitiesStoreFactory
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import com.dev.lbsuhack.feature.activities.util.FakeSessionRepository
import com.dev.lbsuhack.feature.activities.util.MainDispatcherRule
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
class ActivitiesStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionUser = SessionUser(
        id = "101",
        name = "Никита Ковалев",
        schoolName = "School #1",
        pupilClass = "10-А",
    )

    private val sessionRepository = FakeSessionRepository(
        initialState = SessionState.Authenticated(sessionUser),
    )

    private val activityRepository = ActivityRepositoryImpl()

    private fun createStore(): ActivitiesStore = ActivitiesStoreFactory(
        storeFactory = DefaultStoreFactory(),
        activityRepository = activityRepository,
        observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun loadFillsStateWithActivitiesAndUser() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()

        assertFalse(store.state.isLoading)
        assertTrue(store.state.activities.isNotEmpty())
        assertEquals(sessionUser, store.state.currentUser)
    }

    @Test
    fun selectCategoryFiltersActivities() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()

        store.accept(ActivitiesStore.Intent.SelectCategory(ActivityCategory.BOARD_GAMES))
        runCurrent()

        assertEquals(ActivityCategory.BOARD_GAMES, store.state.selectedCategory)
        assertTrue(store.state.filteredActivities.all { it.category == ActivityCategory.BOARD_GAMES })
    }

    @Test
    fun selectTabFiltersActivities() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()

        store.accept(ActivitiesStore.Intent.SelectTab(ActivityTab.MY_PROPOSALS))
        runCurrent()

        assertEquals(ActivityTab.MY_PROPOSALS, store.state.selectedTab)
        assertTrue(store.state.filteredActivities.all { it.authorId == sessionUser.id })
    }

    @Test
    fun clickActivityPublishesOpenDetailsLabel() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.labels.test {
            store.accept(ActivitiesStore.Intent.ClickActivity("act-1"))
            assertEquals(ActivitiesStore.Label.OpenDetails("act-1"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleJoinTogglesParticipation() = runTest(mainDispatcherRule.testDispatcher) {
        val store = createStore()

        store.accept(ActivitiesStore.Intent.Load)
        runCurrent()

        val itemBefore = store.state.activities.find { it.id == "act-4" }
        assertNotNull(itemBefore)
        val initialJoined = itemBefore?.isJoined(sessionUser.id) == true

        store.accept(ActivitiesStore.Intent.ToggleJoin("act-4"))
        runCurrent()

        val itemAfter = store.state.activities.find { it.id == "act-4" }
        assertEquals(!initialJoined, itemAfter?.isJoined(sessionUser.id))
    }
}
