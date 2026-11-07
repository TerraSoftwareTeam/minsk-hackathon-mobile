package com.dev.lbsuhack.core.network.auth

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class EncryptedTokenStorage(
    private val persistence: TokenPersistence,
    private val cipher: TokenCipher,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TokenStorage {

    private val mutex = Mutex()

    private val _authState = MutableStateFlow(AuthState())
    override val authState: StateFlow<AuthState> =
        _authState.asStateFlow()

    private var initialized = false

    suspend fun initialize() {
        withContext(ioDispatcher) {
            mutex.withLock {
                loadIfNeeded()
            }
        }
    }

    override suspend fun getAccessToken(): String? =
        withContext(ioDispatcher) {
            mutex.withLock {
                loadIfNeeded()
                _authState.value.accessToken
            }
        }

    override suspend fun getRefreshToken(): String? =
        withContext(ioDispatcher) {
            mutex.withLock {
                loadIfNeeded()
                _authState.value.refreshToken
            }
        }

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String?,
    ) {
        require(accessToken.isNotBlank()) {
            "Access token must not be blank"
        }
        require(refreshToken == null || refreshToken.isNotBlank()) {
            "Refresh token must be null or non-blank"
        }

        withContext(ioDispatcher) {
            mutex.withLock {
                loadIfNeeded()

                val newState = AuthState(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                )

                val plaintext = encode(newState)
                val encrypted = try {
                    cipher.encrypt(plaintext)
                } finally {
                    plaintext.fill(0)
                }

                persistence.write(encrypted)

                _authState.value = newState
            }
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            mutex.withLock {
                val plaintext = encode(AuthState())

                val encrypted = try {
                    cipher.encrypt(plaintext)
                } finally {
                    plaintext.fill(0)
                }

                persistence.write(encrypted)

                _authState.value = AuthState()
                initialized = true
            }
        }
    }

    private fun loadIfNeeded() {
        if (initialized) return

        val encrypted = persistence.read()
        if (encrypted == null) {
            _authState.value = AuthState()
            initialized = true
            return
        }

        val restored = try {
            val plaintext = cipher.decrypt(encrypted)
            try {
                decode(plaintext)
            } finally {
                plaintext.fill(0)
            }
        } catch (_: Exception) {
            AuthState()
        }

        _authState.value = restored
        initialized = true
    }

    private fun encode(state: AuthState): ByteArray =
        buildJsonObject {
            put("access_token", state.accessToken?.let { JsonPrimitive(it) } ?: JsonNull)
            put("refresh_token", state.refreshToken?.let { JsonPrimitive(it) } ?: JsonNull)
        }
            .toString()
            .toByteArray(Charsets.UTF_8)

    private fun decode(bytes: ByteArray): AuthState {
        val json = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject

        return AuthState(
            accessToken = json.nullableToken("access_token"),
            refreshToken = json.nullableToken("refresh_token"),
        )
    }

    private fun JsonObject.nullableToken(key: String): String? {
        val element = this[key] ?: return null
        if (element is JsonNull) return null
        val token = element.jsonPrimitive.content
        require(token.isNotBlank()) {
            "Stored token must not be blank"
        }
        return token
    }
}
