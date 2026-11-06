package com.dev.lbsuhack.feature.profile.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.ClearSessionUseCase
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.feature.profile.data.ProfileRepositoryImpl
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStore
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStoreFactory
import com.dev.lbsuhack.feature.profile.util.FakeSessionRepository
import com.dev.lbsuhack.feature.profile.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
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

    private val sessionUser = SessionUser(
        id = "7",
        name = "Nikita",
        schoolName = "School #1",
    )

    private val sessionRepository = FakeSessionRepository(
        initialState = SessionState.Authenticated(sessionUser),
    )

    private val profileRepository = ProfileRepositoryImpl()

    private fun createStore(): ProfileStore = ProfileStoreFactory(
        storeFactory = DefaultStoreFactory(),
        profileRepository = profileRepository,
        observeSession = ObserveSessionUseCase(sessionRepository),
        clearSession = ClearSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun `load fills the state with the session user`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(ProfileStore.Intent.Load)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertEquals("Nikita", store.state.name)
            assertEquals("School #1", store.state.schoolName)
            assertEquals(0, store.state.savedCount)
            assertTrue(store.state.notificationsEnabled)
            assertFalse(store.state.canReviewRequests)
        }

    @Test
    fun `teacher can open the requests section`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val teacherRepository = FakeSessionRepository(
                initialState = SessionState.Authenticated(
                    sessionUser.copy(role = UserRole.TEACHER),
                ),
            )
            val store = ProfileStoreFactory(
                storeFactory = DefaultStoreFactory(),
                profileRepository = profileRepository,
                observeSession = ObserveSessionUseCase(teacherRepository),
                clearSession = ClearSessionUseCase(teacherRepository),
            ).create()

            store.accept(ProfileStore.Intent.Load)
            runCurrent()

            assertTrue(store.state.canReviewRequests)
        }

    @Test
    fun `toggle notifications flips the flag`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(ProfileStore.Intent.ToggleNotifications)
            runCurrent()

            assertFalse(store.state.notificationsEnabled)
        }

    @Test
    fun `logout clears the session`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(ProfileStore.Intent.Logout)
            runCurrent()

            assertTrue(sessionRepository.cleared)
        }
}
