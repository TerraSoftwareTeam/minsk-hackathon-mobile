package com.dev.lbsuhack.core.network.client

import com.dev.lbsuhack.core.network.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.github.cdimascio.dotenv.dotenv

object AppHttpClientFactory {

    val dotenv = dotenv()
    private val BASE_URL = dotenv.get("BASE_URL") ?: "null"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun createUnauthenticated(): HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(Logging) { level = LogLevel.INFO }
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }

    fun createAuthenticated(
        tokenStorage: TokenStorage,
        refreshTokens: suspend () -> Boolean,
    ): HttpClient = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
        install(Logging) { level = LogLevel.INFO }
        install(AuthPlugin) {
            this.tokenStorage = tokenStorage
            this.refreshTokens = refreshTokens
            this.isAuthRequest = { url ->
                url.contains("/auth/login") ||
                    url.contains("/auth/refresh") ||
                    url.contains("/auth/register")
            }
        }
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
}
