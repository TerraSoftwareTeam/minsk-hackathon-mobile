package com.dev.lbsuhack.core.network.call

import android.util.Log
import kotlinx.coroutines.CancellationException
import java.net.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import com.dev.lbsuhack.core.network.client.AppHttpClientFactory
import com.dev.lbsuhack.core.network.error.NetworkError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import java.io.IOException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

suspend inline fun <T> safeCall(crossinline block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ClientRequestException) {
        val status = e.response.status.value
        val bodyText = try {
            e.response.bodyAsText()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
        Log.e("HttpClient", "${e.response.call.request.method.value} ${e.response.call.request.url.encodedPath}: HTTP $status")
        val errorMessage = extractErrorMessage(bodyText)
        when (status) {
            401 -> NetworkResult.Error(NetworkError.Unauthorized)
            403 -> NetworkResult.Error(if (errorMessage != null) NetworkError.ClientError(403, errorMessage) else NetworkError.Forbidden)
            404 -> NetworkResult.Error(NetworkError.NotFound)
            else -> NetworkResult.Error(NetworkError.ClientError(status, errorMessage))
        }
    } catch (e: ServerResponseException) {
        Log.e("HttpClient", "${e.response.call.request.method.value} ${e.response.call.request.url.encodedPath}: HTTP ${e.response.status.value}")
        NetworkResult.Error(NetworkError.ServerError(e.response.status.value))
    } catch (e: HttpRequestTimeoutException) {
        NetworkResult.Error(NetworkError.Timeout)
    } catch (e: SocketTimeoutException) {
        NetworkResult.Error(NetworkError.Timeout)
    } catch (e: IOException) {
        Log.e("HttpClient", "Network IO error: ${e.message}", e)
        NetworkResult.Error(NetworkError.NoInternet)
    } catch (e: Exception) {
        Log.e("HttpClient", "Unexpected error: ${e.message}", e)
        NetworkResult.Error(NetworkError.Exception(e))
    }
}

fun extractErrorMessage(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return try {
        val element = AppHttpClientFactory.defaultJson.parseToJsonElement(body)
        val obj = element as? JsonObject ?: return null
        val msgElement = obj["message"] ?: return null
        when (msgElement) {
            is JsonPrimitive -> msgElement.content
            is JsonArray -> msgElement.joinToString("; ") {
                (it as? JsonPrimitive)?.content ?: it.toString()
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}
