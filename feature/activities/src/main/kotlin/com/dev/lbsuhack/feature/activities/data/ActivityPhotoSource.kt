package com.dev.lbsuhack.feature.activities.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall

class ActivityPhotoSource(private val httpClient: HttpClient) {
    suspend fun load(url: String): ByteArray = when (val result = safeCall {
        httpClient.get(url) { expectSuccess = true }.body<ByteArray>()
    }) {
        is NetworkResult.Success -> result.data
        is NetworkResult.Error -> error("Image download failed")
    }
}
