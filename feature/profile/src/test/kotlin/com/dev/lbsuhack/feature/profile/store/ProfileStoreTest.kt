package com.dev.lbsuhack.feature.profile.store

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.push.NotificationSettingsRepository
import com.dev.lbsuhack.core.domain.push.ObserveNotificationsEnabledUseCase
import com.dev.lbsuhack.core.domain.push.SetNotificationsEnabledUseCase
import com.dev.lbsuhack.core.domain.session.ClearSessionUseCase
import com.dev.lbsuhack.core.domain.session.ObserveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionState
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.domain.user.GetCurrentUserUseCase
import com.dev.lbsuhack.core.domain.user.UpdateUserCacheUseCase
import com.dev.lbsuhack.core.domain.user.UserRepository
import com.dev.lbsuhack.feature.profile.domain.ProfileRepository
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStore
import com.dev.lbsuhack.feature.profile.store.profile.ProfileStoreFactory
import com.dev.lbsuhack.feature.profile.util.FakeSessionRepository
import com.dev.lbsuhack.feature.profile.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testUser = User.Pupil(
        id = "7",
        name = "Nikita",
        surname = "Ivanov",
        schoolsById = "3575208",
        schoolName = "School #1",
        pupilClass = "10-А",
    )

    private val sessionUser = SessionUser(
        id = "7",
        name = "Nikita",
        schoolName = "School #1",
    )

    private val sessionRepository = FakeSessionRepository(
        initialState = SessionState.Authenticated(sessionUser),
    )

    private val profileRepository = FakeProfileRepository()

    private val notificationSettings = FakeNotificationSettingsRepository()

    private fun createStore(customUser: User? = testUser, repo: FakeUserRepository? = null): ProfileStore {
        val userRepo = repo ?: FakeUserRepository(customUser)
        return ProfileStoreFactory(
            storeFactory = DefaultStoreFactory(),
            profileRepository = profileRepository,
            observeNotificationsEnabled = ObserveNotificationsEnabledUseCase(notificationSettings),
            setNotificationsEnabled = SetNotificationsEnabledUseCase(notificationSettings),
            getCurrentUser = GetCurrentUserUseCase(userRepo),
            updateUserCache = UpdateUserCacheUseCase(userRepo),
            saveSession = SaveSessionUseCase(sessionRepository),
            clearSession = ClearSessionUseCase(sessionRepository),
            observeSession = ObserveSessionUseCase(sessionRepository),
        ).create()
    }

    @Test
    fun loadFillsTheStateWithTheSessionUser() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(ProfileStore.Intent.Load)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertFalse(store.state.isError)
            assertTrue(store.state.hasUser)
            assertEquals("Nikita Ivanov", store.state.fullName)
            assertEquals("School #1", store.state.schoolName)
            assertEquals("10-А", store.state.pupilClass)
            assertEquals(0, store.state.savedCount)
            assertTrue(store.state.notificationsEnabled)
            assertFalse(store.state.canReviewRequests)
            store.dispose()
        }

    @Test
    fun teacherCanOpenTheRequestsSection() =
        runTest(mainDispatcherRule.testDispatcher) {
            val teacher = User.Teacher(
                id = "8",
                name = "Elena",
                surname = "Smirnova",
                schoolsById = "School #1",
            )
            val store = createStore(teacher)

            store.accept(ProfileStore.Intent.Load)
            runCurrent()

            assertTrue(store.state.canReviewRequests)
            assertEquals(UserRole.TEACHER, store.state.role)
            store.dispose()
        }

    @Test
    fun toggleNotificationsFlipsTheFlag() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(ProfileStore.Intent.ToggleNotifications)
            runCurrent()

            assertFalse(store.state.notificationsEnabled)
            assertFalse(notificationSettings.isEnabled.value)
            store.dispose()
        }

    @Test
    fun rapidDoubleToggleConvergesToTheLatestValue() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(ProfileStore.Intent.ToggleNotifications)
            store.accept(ProfileStore.Intent.ToggleNotifications)
            runCurrent()

            assertTrue(store.state.notificationsEnabled)
            assertTrue(notificationSettings.isEnabled.value)
            store.dispose()
        }

    @Test
    fun toggleNotificationsKeepsTheStateAndPublishesMessageWhenTheOperationFails() =
        runTest(mainDispatcherRule.testDispatcher) {
            notificationSettings.succeeds = false
            val store = createStore()
            val labels = mutableListOf<ProfileStore.Label>()
            val labelJob = launch { store.labels.collect { labels += it } }
            runCurrent()

            store.accept(ProfileStore.Intent.ToggleNotifications)
            runCurrent()
            labelJob.cancel()

            assertTrue(store.state.notificationsEnabled)
            assertTrue(notificationSettings.isEnabled.value)
            assertEquals(1, labels.size)
            store.dispose()
        }

    @Test
    fun logoutClearsTheSession() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(ProfileStore.Intent.Logout)
            runCurrent()

            assertTrue(sessionRepository.cleared)
            assertFalse(store.state.isLoggingOut)
            store.dispose()
        }

    @Test
    fun profileEditPreservesLocalSchoolInProfileAndSessionAndClosesDialog() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(ProfileStore.Intent.Load)
            runCurrent()
            store.accept(ProfileStore.Intent.ClickEdit)
            runCurrent()
            assertNotNull(store.state.dialog)
            store.accept(ProfileStore.Intent.ChangeName("Ivan"))
            store.accept(ProfileStore.Intent.ConfirmEdit)
            runCurrent()

            assertNull(store.state.dialog)
            assertFalse(store.state.isSaving)
            assertEquals("School #1", store.state.schoolName)
            assertEquals("Ivan Ivanov", store.state.fullName)
            val session = sessionRepository.state.value as SessionState.Authenticated
            assertEquals("School #1", session.user.schoolName)
            assertEquals("Ivan", session.user.name)
            assertEquals("10-А", session.user.pupilClass)
            store.dispose()
        }

    @Test
    fun profileEditFailureKeepsDialogVisible() =
        runTest(mainDispatcherRule.testDispatcher) {
            profileRepository.updateResult = DomainResult.Failure(DomainError.Network)
            val store = createStore()
            store.accept(ProfileStore.Intent.Load)
            runCurrent()
            store.accept(ProfileStore.Intent.ClickEdit)
            runCurrent()
            store.accept(ProfileStore.Intent.ChangeName("Ivan"))
            store.accept(ProfileStore.Intent.ConfirmEdit)
            runCurrent()

            assertNotNull(store.state.dialog)
            assertEquals("Ivan", store.state.dialog?.name)
            assertFalse(store.state.isSaving)
            store.dispose()
        }

    @Test
    fun loadFailureWithoutCacheKeepsTheSessionProfile() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeUserRepository(null).apply { refreshFails = true }
            val store = createStore(repo = repo)

            store.accept(ProfileStore.Intent.Load)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertFalse(store.state.isError)
            assertTrue(store.state.hasUser)
            store.dispose()
        }

    @Test
    fun profileIsVisibleFromTheSessionBeforeLoading() {
        val store = createStore()

        assertTrue(store.state.hasUser)
        assertFalse(store.state.isLoading)
        assertTrue(store.state.fullName.isNotBlank())
        store.dispose()
    }

    @Test
    fun localProfileRemainsVisibleWhenRefreshFails() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeUserRepository(testUser).apply { refreshFails = true }
            val store = ProfileStoreFactory(
                DefaultStoreFactory(),
                profileRepository,
                ObserveNotificationsEnabledUseCase(notificationSettings),
                SetNotificationsEnabledUseCase(notificationSettings),
                GetCurrentUserUseCase(repo),
                UpdateUserCacheUseCase(repo),
                SaveSessionUseCase(sessionRepository),
                ClearSessionUseCase(sessionRepository),
                ObserveSessionUseCase(sessionRepository),
            ).create()

            store.accept(ProfileStore.Intent.Load)
            runCurrent()

            assertEquals("School #1", store.state.schoolName)
            assertEquals("Nikita Ivanov", store.state.fullName)
            assertEquals("10-А", store.state.pupilClass)
            assertFalse(store.state.isLoading)
            assertFalse(store.state.isError)
            assertTrue(store.state.hasUser)
            store.dispose()
        }

    private class FakeProfileRepository : ProfileRepository {
        var updateResult: DomainResult<User>? = null

        override suspend fun savedCount(): Int = 0

        override suspend fun updateOwnProfile(
            userId: String,
            name: String,
            surname: String,
        ): DomainResult<User> = updateResult ?: DomainResult.Success(
            User.Pupil(
                id = userId,
                name = name,
                surname = surname,
                pupilClass = "10-А",
            ),
        )
    }

    private class FakeNotificationSettingsRepository : NotificationSettingsRepository {
        var succeeds = true
        private val _enabled = MutableStateFlow(true)
        override val isEnabled: StateFlow<Boolean> = _enabled.asStateFlow()

        override suspend fun setNotificationsEnabled(enabled: Boolean): Boolean {
            if (!succeeds) return false
            _enabled.value = enabled
            return true
        }
    }

    private class FakeUserRepository(initialUser: User? = null) : UserRepository {
        var refreshFails = false
        private val _user = MutableStateFlow(initialUser)
        override val currentUser: StateFlow<User?> = _user.asStateFlow()

        override suspend fun getCurrentUser(forceRefresh: Boolean): DomainResult<User> {
            if (forceRefresh && refreshFails) return DomainResult.Failure(DomainError.Network)
            val user = _user.value ?: return DomainResult.Failure(DomainError.NotFound)
            return DomainResult.Success(user)
        }

        override suspend fun updateCache(user: User) {
            _user.value = user
        }

        override suspend fun clearCache() {
            _user.value = null
        }
    }
}
