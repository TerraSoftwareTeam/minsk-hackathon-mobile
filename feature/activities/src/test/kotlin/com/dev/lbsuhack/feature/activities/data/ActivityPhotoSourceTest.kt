package com.dev.lbsuhack.feature.activities.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityPhotoSourceTest {
    @Test
    fun downloadsRemotePhotoBytes() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        HttpClient(MockEngine { request ->
            assertEquals("https://images.example.test/event.jpg", request.url.toString())
            respond(bytes, HttpStatusCode.OK)
        }).use { client ->
            assertArrayEquals(bytes, ActivityPhotoSource(client).load("https://images.example.test/event.jpg"))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun rejectedPhotoIsNotDecodedAsImage() = runTest {
        HttpClient(MockEngine { respond("Not found", HttpStatusCode.NotFound) }).use { client ->
            ActivityPhotoSource(client).load("https://images.example.test/missing.jpg")
        }
    }
}
