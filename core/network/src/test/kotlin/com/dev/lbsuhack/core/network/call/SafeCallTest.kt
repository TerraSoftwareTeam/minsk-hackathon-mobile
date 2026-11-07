package com.dev.lbsuhack.core.network.call

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import com.dev.lbsuhack.core.network.error.NetworkError
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeCallTest {

    private fun client(handler: MockRequestHandler): HttpClient = HttpClient(MockEngine(handler)) {
        expectSuccess = true
    }

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode,
    ) = respond(
        content = body,
        status = status,
        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
    )

    private suspend fun call(handler: MockRequestHandler): NetworkResult<String> = safeCall {
        client(handler).get("test").bodyAsText()
    }

    @Test
    fun wrapsSuccessfulBlockIntoSuccess() = runTest {
        val result = call {
            respondJson("""{"message":"ok"}""", HttpStatusCode.OK)
        }

        assertEquals(NetworkResult.Success("""{"message":"ok"}"""), result)
    }

    @Test
    fun rethrowsCancellation() = runTest {
        var thrown: Throwable? = null
        try {
            safeCall<String> { throw CancellationException("cancelled") }
        } catch (error: Throwable) {
            thrown = error
        }

        assertNotNull(thrown)
        assertTrue(thrown is CancellationException)
    }

    @Test
    fun mapsClientErrorWithMessage() = runTest {
        val result = call {
            respondJson("""{"message":"Class is full"}""", HttpStatusCode.BadRequest)
        }

        val error = (result as NetworkResult.Error).error
        assertEquals(NetworkError.ClientError(400, "Class is full"), error)
    }

    @Test
    fun mapsClientErrorWithoutMessage() = runTest {
        val result = call {
            respondJson("{}", HttpStatusCode.BadRequest)
        }

        val error = (result as NetworkResult.Error).error
        assertEquals(NetworkError.ClientError(400, null), error)
    }

    @Test
    fun mapsUnauthorized() = runTest {
        val result = call {
            respondJson("""{"message":"expired"}""", HttpStatusCode.Unauthorized)
        }

        assertEquals(NetworkError.Unauthorized, (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsForbiddenWithMessageToClientError() = runTest {
        val result = call {
            respondJson("""{"message":"no rights"}""", HttpStatusCode.Forbidden)
        }

        assertEquals(NetworkError.ClientError(403, "no rights"), (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsForbiddenWithoutMessageToForbidden() = runTest {
        val result = call {
            respondJson("{}", HttpStatusCode.Forbidden)
        }

        assertEquals(NetworkError.Forbidden, (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsNotFound() = runTest {
        val result = call {
            respondJson("{}", HttpStatusCode.NotFound)
        }

        assertEquals(NetworkError.NotFound, (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsMessageArrayToJoinedString() = runTest {
        val result = call {
            respondJson(
                """{"message":["first","second"]}""",
                HttpStatusCode.UnprocessableEntity,
            )
        }

        assertEquals(
            NetworkError.ClientError(422, "first; second"),
            (result as NetworkResult.Error).error,
        )
    }

    @Test
    fun mapsNonJsonErrorBodyToNullMessage() = runTest {
        val result = call {
            respondJson("<html>oops</html>", HttpStatusCode.Conflict)
        }

        assertEquals(NetworkError.ClientError(409, null), (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsServerError() = runTest {
        val result = call {
            respondJson("{}", HttpStatusCode.InternalServerError)
        }

        assertEquals(NetworkError.ServerError(500), (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsRequestTimeout() = runTest {
        val result = call { throw HttpRequestTimeoutException(HttpRequestBuilder()) }

        assertEquals(NetworkError.Timeout, (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsSocketTimeout() = runTest {
        val result = call { throw SocketTimeoutException() }

        assertEquals(NetworkError.Timeout, (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsIoFailureToNoInternet() = runTest {
        val result = call { throw java.io.IOException("offline") }

        assertEquals(NetworkError.NoInternet, (result as NetworkResult.Error).error)
    }

    @Test
    fun mapsUnexpectedException() = runTest {
        val result = call { throw IllegalStateException("boom") }

        val error = (result as NetworkResult.Error).error
        assertTrue(error is NetworkError.Exception)
        assertEquals("boom", (error as NetworkError.Exception).cause.message)
    }
}
