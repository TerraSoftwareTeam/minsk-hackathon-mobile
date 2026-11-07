package com.dev.lbsuhack.core.data.push

import com.dev.lbsuhack.core.data.FakeKeyValueStorage
import com.dev.lbsuhack.core.domain.push.PushTokenProvider
import com.dev.lbsuhack.core.domain.push.PushTokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNotificationSettingsRepositoryTest {

    private class FakePushTokenProvider(private val token: String?) : PushTokenProvider {
        override suspend fun currentToken(): String? = token
    }

    private class FakePushTokenRepository : PushTokenRepository {
        var registerResult = true
        var unregisterResult = true
        val registered = mutableListOf<String>()
        val unregistered = mutableListOf<String>()

        override suspend fun register(token: String): Boolean {
            registered += token
            return registerResult
        }

        override suspend fun unregister(token: String): Boolean {
            unregistered += token
            return unregisterResult
        }
    }

    private fun TestScope.repository(
        storage: FakeKeyValueStorage = FakeKeyValueStorage(),
        provider: FakePushTokenProvider = FakePushTokenProvider("push-token"),
        pushRepository: FakePushTokenRepository = FakePushTokenRepository(),
    ): DefaultNotificationSettingsRepository = DefaultNotificationSettingsRepository(
        storage = storage,
        pushTokenProvider = provider,
        pushTokenRepository = pushRepository,
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun isEnabledDefaultsToTrueWithoutStoredValue() = runTest {
        val repository = repository()

        assertTrue(repository.isEnabled.first())
    }

    @Test
    fun readsTheStoredFlagAtCreation() = runTest {
        val repository = repository(
            storage = FakeKeyValueStorage(mapOf("notifications_enabled" to "false")),
        )

        assertFalse(repository.isEnabled.first())
    }

    @Test
    fun setNotificationsEnabledRegistersTokenAndPersistsTheFlag() = runTest {
        val storage = FakeKeyValueStorage()
        val pushRepository = FakePushTokenRepository()
        val repository = repository(storage = storage, pushRepository = pushRepository)

        assertTrue(repository.setNotificationsEnabled(true))

        assertEquals(listOf("push-token"), pushRepository.registered)
        assertTrue(pushRepository.unregistered.isEmpty())
        assertEquals("true", storage.values["notifications_enabled"])
        assertTrue(repository.isEnabled.first())
    }

    @Test
    fun setNotificationsDisabledUnregistersTokenAndPersistsTheFlag() = runTest {
        val storage = FakeKeyValueStorage()
        val pushRepository = FakePushTokenRepository()
        val repository = repository(storage = storage, pushRepository = pushRepository)

        assertTrue(repository.setNotificationsEnabled(false))

        assertEquals(listOf("push-token"), pushRepository.unregistered)
        assertTrue(pushRepository.registered.isEmpty())
        assertEquals("false", storage.values["notifications_enabled"])
        assertFalse(repository.isEnabled.first())
    }

    @Test
    fun keepsTheFlagWhenTheServerRejects() = runTest {
        val storage = FakeKeyValueStorage()
        val pushRepository = FakePushTokenRepository().apply { registerResult = false }
        val repository = repository(storage = storage, pushRepository = pushRepository)

        assertFalse(repository.setNotificationsEnabled(true))

        assertTrue(pushRepository.registered.isNotEmpty())
        assertFalse(storage.values.containsKey("notifications_enabled"))
        assertTrue(repository.isEnabled.first())
    }

    @Test
    fun failsWithoutDeviceToken() = runTest {
        val storage = FakeKeyValueStorage()
        val pushRepository = FakePushTokenRepository()
        val repository = repository(
            storage = storage,
            provider = FakePushTokenProvider(null),
            pushRepository = pushRepository,
        )

        assertFalse(repository.setNotificationsEnabled(false))

        assertTrue(pushRepository.unregistered.isEmpty())
        assertFalse(storage.values.containsKey("notifications_enabled"))
    }
}
