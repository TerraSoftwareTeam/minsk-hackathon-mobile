package com.dev.lbsuhack.core.data.push

import com.dev.lbsuhack.core.data.FakeKeyValueStorage
import com.dev.lbsuhack.core.data.FakeTokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultPushTokenRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val requestBodies = mutableListOf<String>()
    private var requestCount = 0

    private fun TestScope.repository(
        storage: FakeKeyValueStorage = FakeKeyValueStorage(),
        tokenStorage: FakeTokenStorage = FakeTokenStorage(accessToken = "access"),
        handler: MockRequestHandler,
    ): DefaultPushTokenRepository {
        val client = HttpClient(MockEngine(handler)) {
            expectSuccess = false
            install(ContentNegotiation) { json(json) }
        }
        return DefaultPushTokenRepository(
            storage = storage,
            httpClient = client,
            tokenStorage = tokenStorage,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
    }

    private fun MockRequestHandleScope.respondOk(status: HttpStatusCode = HttpStatusCode.OK) = respond(
        content = "{}",
        status = status,
        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
    )

    private fun captureBody(request: HttpRequestData) {
        requestCount += 1
        requestBodies += (request.body as TextContent).text
    }

    @Test
    fun returnsFalseWithoutRequestWhenAccessTokenMissing() = runTest {
        val repository = repository(
            tokenStorage = FakeTokenStorage(accessToken = null),
            handler = {
                captureBody(it)
                respondOk()
            },
        )

        assertFalse(repository.register("push-token"))

        assertEquals(0, requestCount)
    }

    @Test
    fun returnsFalseWhenAccessTokenBlank() = runTest {
        val repository = repository(
            tokenStorage = FakeTokenStorage(accessToken = " "),
            handler = {
                captureBody(it)
                respondOk()
            },
        )

        assertFalse(repository.unregister("push-token"))

        assertEquals(0, requestCount)
    }

    @Test
    fun registerPostsTokenWithStableDeviceId() = runTest {
        val storage = FakeKeyValueStorage()
        val repository = repository(
            storage = storage,
            handler = {
                captureBody(it)
                respondOk()
            },
        )

        assertTrue(repository.register("first"))
        assertTrue(repository.register("second"))

        assertEquals(2, requestCount)
        val first = json.parseToJsonElement(requestBodies[0]).jsonObject
        val second = json.parseToJsonElement(requestBodies[1]).jsonObject
        assertEquals("first", first["token"]!!.jsonPrimitive.content)
        assertEquals("second", second["token"]!!.jsonPrimitive.content)
        assertEquals("ANDROID", first["platform"]!!.jsonPrimitive.content)
        val firstDeviceId = first["deviceId"]!!.jsonPrimitive.content
        assertEquals(firstDeviceId, second["deviceId"]!!.jsonPrimitive.content)
        assertEquals(firstDeviceId, storage.getString("device_id"))
    }

    @Test
    fun registerReturnsFalseWhenServerRejects() = runTest {
        val repository = repository(
            handler = {
                captureBody(it)
                respondOk(HttpStatusCode.BadRequest)
            },
        )

        assertFalse(repository.register("push-token"))
    }

    @Test
    fun unregisterDeletesToken() = runTest {
        val repository = repository(
            handler = {
                captureBody(it)
                respondOk(HttpStatusCode.NoContent)
            },
        )

        assertTrue(repository.unregister("push-token"))

        val body = json.parseToJsonElement(requestBodies[0]).jsonObject
        assertEquals("push-token", body["token"]!!.jsonPrimitive.content)
    }

    @Test
    fun swallowsNetworkFailure() = runTest {
        val repository = repository(
            handler = { throw IOException("offline") },
        )

        assertFalse(repository.register("push-token"))
        assertFalse(repository.unregister("push-token"))
    }

    @Test
    fun rethrowsCancellation() = runTest {
        val repository = repository(
            handler = { throw CancellationException("cancelled") },
        )

        var thrown: Throwable? = null
        try {
            repository.register("push-token")
        } catch (error: Throwable) {
            thrown = error
        }

        assertNotNull(thrown)
        assertTrue(thrown is CancellationException)
    }
}
