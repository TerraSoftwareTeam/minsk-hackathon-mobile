package com.dev.lbsuhack.core.network.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenRefresherTest {

    private var requestCount = 0
    private var requestBody: String? = null

    private fun refresher(
        tokenStorage: FakeTokenStorage,
        handler: MockRequestHandler,
    ): TokenRefresher {
        val client = HttpClient(MockEngine(handler)) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return TokenRefresher(
            tokenStorage = tokenStorage,
            unauthenticatedClientProvider = { client },
        )
    }

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData = respond(
        content = body,
        status = status,
        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
    )

    private fun capture(request: HttpRequestData) {
        requestCount += 1
        requestBody = (request.body as TextContent).text
    }

    @Test
    fun returnsFalseWithoutRequestWhenRefreshTokenMissing() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = null)
        val refresher = refresher(tokenStorage) {
            capture(it)
            respondJson("{}")
        }

        assertFalse(refresher.refresh())

        assertEquals(0, requestCount)
    }

    @Test
    fun refreshesAndStoresRotatedTokens() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = "r1")
        val refresher = refresher(tokenStorage) {
            capture(it)
            respondJson("""{"data":{"access_token":"a2","refresh_token":"r2"}}""")
        }

        assertTrue(refresher.refresh())

        assertEquals(1, tokenStorage.savedTokens.size)
        assertEquals("a2" to "r2", tokenStorage.savedTokens.single())
        val body = Json.parseToJsonElement(requestBody!!).jsonObject
        assertEquals("r1", body["refresh_token"]!!.jsonPrimitive.content)
    }

    @Test
    fun keepsPreviousRefreshTokenWhenResponseOmitsIt() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = "r1")
        val refresher = refresher(tokenStorage) {
            capture(it)
            respondJson("""{"data":{"access_token":"a2"}}""")
        }

        assertTrue(refresher.refresh())

        assertEquals("a2" to "r1", tokenStorage.savedTokens.single())
    }

    @Test
    fun clearsTokensOnUnauthorized() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = "r1")
        val refresher = refresher(tokenStorage) {
            capture(it)
            respondJson("{}", HttpStatusCode.Unauthorized)
        }

        assertFalse(refresher.refresh())

        assertEquals(1, tokenStorage.clearedCount)
        assertEquals(0, tokenStorage.savedTokens.size)
    }

    @Test
    fun clearsTokensOnForbidden() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = "r1")
        val refresher = refresher(tokenStorage) {
            capture(it)
            respondJson("{}", HttpStatusCode.Forbidden)
        }

        assertFalse(refresher.refresh())

        assertEquals(1, tokenStorage.clearedCount)
    }

    @Test
    fun keepsTokensOnServerError() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = "r1")
        val refresher = refresher(tokenStorage) {
            capture(it)
            respondJson("{}", HttpStatusCode.InternalServerError)
        }

        assertFalse(refresher.refresh())

        assertEquals(0, tokenStorage.clearedCount)
        assertEquals(0, tokenStorage.savedTokens.size)
    }

    @Test
    fun returnsFalseOnNetworkFailure() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = "r1")
        val refresher = refresher(tokenStorage) { throw IOException("offline") }

        assertFalse(refresher.refresh())

        assertEquals(0, tokenStorage.savedTokens.size)
        assertEquals(0, tokenStorage.clearedCount)
    }

    @Test
    fun returnsFalseOnMalformedSuccessBody() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = "r1")
        val refresher = refresher(tokenStorage) {
            capture(it)
            respondJson("not-json")
        }

        assertFalse(refresher.refresh())

        assertEquals(0, tokenStorage.savedTokens.size)
    }

    @Test
    fun rethrowsCancellation() = runTest {
        val tokenStorage = FakeTokenStorage(accessToken = "a1", refreshToken = "r1")
        val refresher = refresher(tokenStorage) { throw CancellationException("cancelled") }

        var thrown: Throwable? = null
        try {
            refresher.refresh()
        } catch (error: Throwable) {
            thrown = error
        }

        assertTrue(thrown is CancellationException)
        assertEquals(0, tokenStorage.savedTokens.size)
    }
}
