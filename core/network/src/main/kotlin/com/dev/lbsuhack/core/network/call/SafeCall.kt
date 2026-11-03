package com.dev.lbsuhack.core.network.call

import com.dev.lbsuhack.core.network.error.NetworkError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import java.io.IOException

suspend inline fun <T> safeCall(crossinline block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e: ClientRequestException) {
        when (e.response.status.value) {
            401 -> NetworkResult.Error(NetworkError.Unauthorized)
            403 -> NetworkResult.Error(NetworkError.Forbidden)
            404 -> NetworkResult.Error(NetworkError.NotFound)
            else -> NetworkResult.Error(NetworkError.Unknown)
        }
    } catch (e: ServerResponseException) {
        NetworkResult.Error(NetworkError.ServerError(e.response.status.value))
    } catch (e: IOException) {
        NetworkResult.Error(NetworkError.Timeout)
    } catch (e: Exception) {
        NetworkResult.Error(NetworkError.Exception(e))
    }
}
