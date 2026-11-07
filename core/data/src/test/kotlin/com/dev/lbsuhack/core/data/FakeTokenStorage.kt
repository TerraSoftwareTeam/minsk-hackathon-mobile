package com.dev.lbsuhack.core.data

import com.dev.lbsuhack.core.network.auth.AuthState
import com.dev.lbsuhack.core.network.auth.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTokenStorage(
    accessToken: String? = null,
    refreshToken: String? = null,
) : TokenStorage {

    var clearedCount = 0
        private set

    val savedAccessTokens = mutableListOf<String>()
    val savedRefreshTokens = mutableListOf<String?>()

    private val state = MutableStateFlow(AuthState(accessToken, refreshToken))

    override val authState: StateFlow<AuthState> = state.asStateFlow()

    override suspend fun getAccessToken(): String? = state.value.accessToken

    override suspend fun getRefreshToken(): String? = state.value.refreshToken

    override suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        savedAccessTokens += accessToken
        savedRefreshTokens += refreshToken
        state.value = AuthState(accessToken, refreshToken)
    }

    override suspend fun clear() {
        clearedCount += 1
        state.value = AuthState()
    }

    fun updateTokens(accessToken: String?, refreshToken: String? = null) {
        state.value = AuthState(accessToken, refreshToken)
    }
}
