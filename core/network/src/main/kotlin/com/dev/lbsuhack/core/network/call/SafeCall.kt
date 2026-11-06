package com.dev.lbsuhack.core.network.call

import android.util.Log
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
    } catch (e: ClientRequestException) {
        val status = e.response.status.value
        val bodyText = runCatching { e.response.bodyAsText() }.getOrNull()
        Log.e("HttpClient", "Client error $status: $bodyText", e)
        val errorMessage = extractErrorMessage(bodyText)
        when (status) {
            401 -> NetworkResult.Error(NetworkError.Unauthorized)
            403 -> NetworkResult.Error(if (errorMessage != null) NetworkError.ClientError(403, errorMessage) else NetworkError.Forbidden)
            404 -> NetworkResult.Error(NetworkError.NotFound)
            else -> NetworkResult.Error(NetworkError.ClientError(status, errorMessage))
        }
    } catch (e: ServerResponseException) {
        val bodyText = runCatching { e.response.bodyAsText() }.getOrNull()
        Log.e("HttpClient", "Server error ${e.response.status.value}: $bodyText", e)
        NetworkResult.Error(NetworkError.ServerError(e.response.status.value))
    } catch (e: IOException) {
        Log.e("HttpClient", "Network IO error: ${e.message}", e)
        NetworkResult.Error(NetworkError.Timeout)
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
