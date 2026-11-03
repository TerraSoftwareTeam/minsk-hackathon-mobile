package com.dev.lbsuhack.core.network.client

import com.dev.lbsuhack.core.network.auth.TokenStorage
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthPluginConfig {
    lateinit var tokenStorage: TokenStorage
    lateinit var refreshTokens: suspend () -> Boolean
    var isAuthRequest: (String) -> Boolean = { false }
}

val AuthPlugin = createClientPlugin("AuthPlugin", ::AuthPluginConfig) {
    val tokenStorage = pluginConfig.tokenStorage
    val refreshTokens = pluginConfig.refreshTokens
    val isAuthRequest = pluginConfig.isAuthRequest
    val refreshMutex = Mutex()

    suspend fun HttpRequestBuilder.applyAccessToken() {
        headers.remove(HttpHeaders.Authorization)
        tokenStorage.getAccessToken()
            ?.takeIf(String::isNotBlank)
            ?.let { headers.append(HttpHeaders.Authorization, "Bearer $it") }
    }

    on(Send) { request ->
        if (isAuthRequest(request.url.toString())) {
            return@on proceed(request)
        }

        request.applyAccessToken()
        var call = proceed(request)
        if (call.response.status != HttpStatusCode.Unauthorized) {
            return@on call
        }

        val tokenWasRefreshed = refreshMutex.withLock {
            val currentToken = tokenStorage.getAccessToken()
            val tokenUsedForRequest = request.headers[HttpHeaders.Authorization]
                ?.removePrefix("Bearer ")

            if (currentToken != null && currentToken != tokenUsedForRequest) {
                true
            } else {
                refreshTokens()
            }
        }

        if (!tokenWasRefreshed) return@on call

        request.applyAccessToken()
        call = proceed(request)
        call
    }
}
