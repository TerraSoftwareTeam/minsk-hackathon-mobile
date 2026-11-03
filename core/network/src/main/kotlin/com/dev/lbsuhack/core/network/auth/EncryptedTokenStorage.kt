package com.dev.lbsuhack.core.network.auth

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EncryptedTokenStorage(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TokenStorage {

    private val mutex = Mutex()
    private val cipher = TokenCipher()

    private val tokenFile = AtomicFile(
        File(
            context.applicationContext.noBackupFilesDir,
            "auth_tokens.enc",
        )
    )

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
                    refreshToken = refreshToken
                        ?: _authState.value.refreshToken,
                )

                val plaintext = encode(newState)
                val encrypted = try {
                    cipher.encrypt(plaintext)
                } finally {
                    plaintext.fill(0)
                }

                writeAtomically(encrypted)

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

                writeAtomically(encrypted)

                _authState.value = AuthState()
                initialized = true
            }
        }
    }

    private fun loadIfNeeded() {
        if (initialized) return

        val encrypted = try {
            tokenFile.openRead().use { it.readBytes() }
        } catch (_: FileNotFoundException) {
            _authState.value = AuthState()
            initialized = true
            return
        }

        val plaintext = cipher.decrypt(encrypted)

        val restored = try {
            decode(plaintext)
        } finally {
            plaintext.fill(0)
        }

        _authState.value = restored
        initialized = true
    }

    private fun writeAtomically(bytes: ByteArray) {
        val stream = tokenFile.startWrite()

        try {
            stream.write(bytes)
            tokenFile.finishWrite(stream)
        } catch (error: Throwable) {
            tokenFile.failWrite(stream)
            throw error
        }
    }

    private fun encode(state: AuthState): ByteArray =
        JSONObject()
            .put(
                "access_token",
                state.accessToken ?: JSONObject.NULL,
            )
            .put(
                "refresh_token",
                state.refreshToken ?: JSONObject.NULL,
            )
            .toString()
            .toByteArray(Charsets.UTF_8)

    private fun decode(bytes: ByteArray): AuthState {
        val json = JSONObject(bytes.toString(Charsets.UTF_8))

        return AuthState(
            accessToken = json.nullableToken("access_token"),
            refreshToken = json.nullableToken("refresh_token"),
        )
    }

    private fun JSONObject.nullableToken(key: String): String? =
        if (isNull(key)) {
            null
        } else {
            getString(key).also {
                require(it.isNotBlank()) {
                    "Stored token must not be blank"
                }
            }
        }
}