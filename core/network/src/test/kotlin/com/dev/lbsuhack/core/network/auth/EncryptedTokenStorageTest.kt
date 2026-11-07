package com.dev.lbsuhack.core.network.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EncryptedTokenStorageTest {

    private fun TestScope.storage(
        persistence: FakeTokenPersistence = FakeTokenPersistence(),
        cipher: FakeTokenCipher = FakeTokenCipher(),
    ): EncryptedTokenStorage = EncryptedTokenStorage(
        persistence = persistence,
        cipher = cipher,
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    private fun storedJson(persistence: FakeTokenPersistence) =
        Json.parseToJsonElement(
            persistence.stored!!.toString(Charsets.UTF_8)
        ).jsonObject

    @Test
    fun startsEmptyWhenFileMissing() = runTest {
        val persistence = FakeTokenPersistence(stored = null)
        val storage = storage(persistence)

        assertNull(storage.getAccessToken())
        assertNull(storage.getRefreshToken())
        assertEquals(AuthState(), storage.authState.value)
    }

    @Test
    fun readsPersistenceOnlyOnce() = runTest {
        val persistence = FakeTokenPersistence(stored = null)
        val storage = storage(persistence)

        storage.getAccessToken()
        storage.getRefreshToken()
        storage.getAccessToken()

        assertEquals(1, persistence.readCount)
    }

    @Test
    fun restoresTokensFromPersistenceOnFirstAccess() = runTest {
        val persistence = FakeTokenPersistence(
            stored = """{"access_token":"a1","refresh_token":"r1"}""".toByteArray(),
        )
        val storage = storage(persistence)

        assertEquals("a1", storage.getAccessToken())
        assertEquals("r1", storage.getRefreshToken())
    }

    @Test
    fun roundtripsSavedTokens() = runTest {
        val persistence = FakeTokenPersistence()
        val storage = storage(persistence)

        storage.saveTokens(accessToken = "a1", refreshToken = "r1")

        assertEquals("a1", storage.getAccessToken())
        assertEquals("r1", storage.getRefreshToken())
        assertEquals(AuthState("a1", "r1"), storage.authState.value)

        val persisted = storedJson(persistence)
        assertEquals("a1", persisted["access_token"]!!.jsonPrimitive.content)
        assertEquals("r1", persisted["refresh_token"]!!.jsonPrimitive.content)
    }

    @Test
    fun persistsExplicitNullRefreshToken() = runTest {
        val persistence = FakeTokenPersistence()
        val storage = storage(persistence)

        storage.saveTokens(accessToken = "a1", refreshToken = null)

        assertNull(storage.getRefreshToken())
        assertTrue(storedJson(persistence)["refresh_token"] is JsonNull)
    }

    @Test
    fun rejectsBlankAccessToken() = runTest {
        val persistence = FakeTokenPersistence()
        val storage = storage(persistence)

        var thrown: Throwable? = null
        try {
            storage.saveTokens(accessToken = " ", refreshToken = "r1")
        } catch (error: Throwable) {
            thrown = error
        }

        assertTrue(thrown is IllegalArgumentException)
        assertEquals(AuthState(), storage.authState.value)
        assertEquals(0, persistence.writeCount)
    }

    @Test
    fun rejectsBlankRefreshToken() = runTest {
        val persistence = FakeTokenPersistence()
        val storage = storage(persistence)

        var thrown: Throwable? = null
        try {
            storage.saveTokens(accessToken = "a1", refreshToken = " ")
        } catch (error: Throwable) {
            thrown = error
        }

        assertTrue(thrown is IllegalArgumentException)
        assertEquals(0, persistence.writeCount)
    }

    @Test
    fun resetsToEmptyStateOnCorruptedPayload() = runTest {
        val persistence = FakeTokenPersistence(
            stored = "not a json payload".toByteArray(),
        )
        val storage = storage(persistence)

        assertNull(storage.getAccessToken())
        assertNull(storage.getRefreshToken())
        assertEquals(AuthState(), storage.authState.value)
    }

    @Test
    fun resetsToEmptyStateWhenCipherKeyUnavailable() = runTest {
        val persistence = FakeTokenPersistence(
            stored = "anything".toByteArray(),
        )
        val storage = storage(
            persistence = persistence,
            cipher = FakeTokenCipher().apply { failDecrypt = true },
        )

        assertNull(storage.getAccessToken())
        assertEquals(AuthState(), storage.authState.value)
    }

    @Test
    fun resetsToEmptyStateOnBlankStoredToken() = runTest {
        val persistence = FakeTokenPersistence(
            stored = """{"access_token":"  ","refresh_token":null}""".toByteArray(),
        )
        val storage = storage(persistence)

        assertNull(storage.getAccessToken())
        assertEquals(AuthState(), storage.authState.value)
    }

    @Test
    fun clearWritesEmptyStateAndKeepsNoTokens() = runTest {
        val persistence = FakeTokenPersistence()
        val storage = storage(persistence)
        storage.saveTokens(accessToken = "a1", refreshToken = "r1")

        storage.clear()

        assertNull(storage.getAccessToken())
        assertNull(storage.getRefreshToken())
        assertEquals(AuthState(), storage.authState.value)

        val persisted = storedJson(persistence)
        assertTrue(persisted["access_token"] is JsonNull)
        assertTrue(persisted["refresh_token"] is JsonNull)
        assertFalse(persistence.stored!!.isEmpty())
    }

    @Test
    fun doesNotRereadPersistenceAfterClear() = runTest {
        val persistence = FakeTokenPersistence()
        val storage = storage(persistence)
        storage.clear()

        storage.getAccessToken()

        assertEquals(0, persistence.readCount)
    }
}
