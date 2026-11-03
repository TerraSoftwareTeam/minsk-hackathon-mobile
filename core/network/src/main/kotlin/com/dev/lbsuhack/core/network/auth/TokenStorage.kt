package com.dev.lbsuhack.core.network.auth

import kotlinx.coroutines.flow.StateFlow

interface TokenStorage {
    val authState: StateFlow<AuthState>

    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?

    suspend fun saveTokens(accessToken: String, refreshToken: String?)
    suspend fun clear()
}

data class AuthState(
    val accessToken: String? = null,
    val refreshToken: String? = null,
)

