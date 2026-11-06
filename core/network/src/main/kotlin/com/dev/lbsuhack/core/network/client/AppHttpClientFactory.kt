package com.dev.lbsuhack.core.network.client

import android.util.Log
import com.dev.lbsuhack.core.domain.localization.LocaleProvider
import com.dev.lbsuhack.core.network.BuildConfig
import com.dev.lbsuhack.core.network.auth.TokenStorage
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import com.dev.lbsuhack.core.network.serialization.BigDecimalSerializer

object AppHttpClientFactory {

    private val BASE_URL = BuildConfig.BASE_URL

    val defaultJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
        serializersModule = SerializersModule {
            contextual(BigDecimalSerializer)
        }
    }

    private val logLevel = if (BuildConfig.DEBUG) LogLevel.HEADERS else LogLevel.NONE
    private val httpLogger = object : Logger {
        override fun log(message: String) {
            Log.d("HttpClient", message)
        }
    }

    fun createUnauthenticated(localeProvider: LocaleProvider): HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(ContentNegotiation) { json(defaultJson) }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) {
            logger = httpLogger
            level = logLevel
            sanitizeHeader { it == HttpHeaders.Authorization || it == HttpHeaders.Cookie || it == HttpHeaders.SetCookie }
        }
        defaultRequest {
            url(BASE_URL)
            header(HttpHeaders.AcceptLanguage, localeProvider.currentLanguageTag())
        }
    }

    fun createAuthenticated(
        localeProvider: LocaleProvider,
        tokenStorage: TokenStorage,
        refreshTokens: suspend () -> Boolean,
    ): HttpClient = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) { json(defaultJson) }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) {
            logger = httpLogger
            level = logLevel
            sanitizeHeader { it == HttpHeaders.Authorization || it == HttpHeaders.Cookie || it == HttpHeaders.SetCookie }
        }
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
            header(HttpHeaders.AcceptLanguage, localeProvider.currentLanguageTag())
        }
    }
}
