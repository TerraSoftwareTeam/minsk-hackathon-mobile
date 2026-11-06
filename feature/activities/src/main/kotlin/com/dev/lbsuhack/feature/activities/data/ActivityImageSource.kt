package com.dev.lbsuhack.feature.activities.data

import android.content.ContentResolver
import android.net.Uri

fun interface ActivityImageSource {
    fun read(uri: String): ActivityImage
}

class AndroidActivityImageSource(private val resolver: ContentResolver) : ActivityImageSource {
    override fun read(uri: String): ActivityImage {
        val parsed = Uri.parse(uri)
        require(parsed.scheme == "content")
        val type = resolver.getType(parsed)
        require(type in setOf("image/jpeg", "image/png", "image/webp", "image/gif"))
        val bytes = requireNotNull(resolver.openInputStream(parsed)).use { stream ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                require(output.size() + count <= 5 * 1024 * 1024)
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        require(bytes.isNotEmpty())
        return ActivityImage(bytes, requireNotNull(type))
    }
}

data class ActivityImage(val bytes: ByteArray, val contentType: String)
