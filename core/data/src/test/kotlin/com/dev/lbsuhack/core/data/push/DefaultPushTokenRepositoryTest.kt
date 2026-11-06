package com.dev.lbsuhack.core.data.push

import com.dev.lbsuhack.core.network.auth.AuthState
import com.dev.lbsuhack.core.network.auth.TokenStorage
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultPushTokenRepositoryTest {

    private class Recorded(val method: HttpMethod, val path: String, val auth: String?, val body: String)

    private class FakeTokenStorage(var token: String?) : TokenStorage {
        override val authState: StateFlow<AuthState> = MutableStateFlow(AuthState())
        override suspend fun getAccessToken(): String? = token
        override suspend fun getRefreshToken(): String? = null
        override suspend fun saveTokens(accessToken: String, refreshToken: String?) {
            token = accessToken
        }
        override suspend fun clear() {
            token = null
        }
    }

    private val recorded = mutableListOf<Recorded>()

    private fun TestScope.repository(token: String?, status: HttpStatusCode = HttpStatusCode.NoContent) =
        DefaultPushTokenRepository(
            httpClient = HttpClient(MockEngine { request ->
                recorded += Recorded(
                    method = request.method,
                    path = request.url.encodedPath,
                    auth = request.headers[HttpHeaders.Authorization],
                    body = request.body.toByteArray().decodeToString(),
                )
                respond("", status)
            }) {
                expectSuccess = false
                install(ContentNegotiation) { json(AppHttpClientFactory.defaultJson) }
                defaultRequest { url("https://api.test/") }
            },
            tokenStorage = FakeTokenStorage(token),
            deviceId = { "device-1" },
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

    @Test
    fun registerPostsTokenWithBearerAndDevice() = runTest {
        assertTrue(repository("jwt").register("fcm-token"))

        val call = recorded.single()
        assertEquals(HttpMethod.Post, call.method)
        assertEquals("/api/v1/device-token", call.path)
        assertEquals("Bearer jwt", call.auth)
        val body = AppHttpClientFactory.defaultJson.parseToJsonElement(call.body).jsonObject
        assertEquals("fcm-token", body["token"]?.jsonPrimitive?.content)
        assertEquals("ANDROID", body["platform"]?.jsonPrimitive?.content)
        assertEquals("device-1", body["deviceId"]?.jsonPrimitive?.content)
    }

    @Test
    fun unregisterDeletesTokenWithBearer() = runTest {
        assertTrue(repository("jwt").unregister("fcm-token"))

        val call = recorded.single()
        assertEquals(HttpMethod.Delete, call.method)
        assertEquals("/api/v1/device-token", call.path)
        assertEquals("Bearer jwt", call.auth)
        val body = AppHttpClientFactory.defaultJson.parseToJsonElement(call.body).jsonObject
        assertEquals("fcm-token", body["token"]?.jsonPrimitive?.content)
    }

    @Test
    fun nothingIsSentWithoutAccessToken() = runTest {
        assertFalse(repository(null).register("fcm-token"))
        assertFalse(repository("").unregister("fcm-token"))
        assertTrue(recorded.isEmpty())
    }

    @Test
    fun rejectedRequestReturnsFalse() = runTest {
        assertFalse(repository("expired", HttpStatusCode.Unauthorized).unregister("fcm-token"))
    }
}
