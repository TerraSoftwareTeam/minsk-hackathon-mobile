package com.dev.lbsuhack.core.network.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class AuthTokensDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
)

@Serializable
data class AuthResponseDto(
    val data: AuthTokensDto,
)

class TokenRefresher(
    private val tokenStorage: TokenStorage,
    private val unauthenticatedClientProvider: () -> HttpClient,
    private val refreshPath: String = "auth/refresh",
) {

    suspend fun refresh(): Boolean {
        val refreshToken = tokenStorage.getRefreshToken() ?: return false

        return try {
            val response = unauthenticatedClientProvider().post(refreshPath) {
                setBody(RefreshRequest(refreshToken = refreshToken))
            }

            if (response.status.value !in 200..299) {
                if (response.status == HttpStatusCode.Unauthorized ||
                    response.status == HttpStatusCode.Forbidden
                ) {
                    tokenStorage.clear()
                }
                return false
            }

            val tokens = response.body<AuthResponseDto>().data
            tokenStorage.saveTokens(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken ?: refreshToken,
            )
            true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            false
        }
    }
}

