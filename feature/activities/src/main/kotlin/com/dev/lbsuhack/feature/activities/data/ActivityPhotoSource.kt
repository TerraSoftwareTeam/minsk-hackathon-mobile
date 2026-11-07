package com.dev.lbsuhack.feature.activities.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readRemaining
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import kotlinx.io.readByteArray

class ActivityPhotoSource(private val httpClient: HttpClient) {

    private val cache = MemoryLruCache<String, ByteArray>(maxSize = MAX_CACHE_BYTES) { it.size }
    suspend fun load(url: String): ByteArray {
        cache.get(url)?.let { return it }
        val bytes = when (val result = safeCall {
            val channel = httpClient.get(url) { expectSuccess = true }.bodyAsChannel()
            channel.readRemaining(MAX_PHOTO_BYTES + 1).readByteArray()
        }) {
            is NetworkResult.Success -> result.data.takeIf { it.size <= MAX_PHOTO_BYTES }
                ?: error("Image exceeds the size limit")

            is NetworkResult.Error -> error("Image download failed")
        }
        cache.put(url, bytes)
        return bytes
    }
}

private const val MAX_PHOTO_BYTES = 5L * 1024 * 1024
private const val MAX_CACHE_BYTES = 16 * 1024 * 1024
