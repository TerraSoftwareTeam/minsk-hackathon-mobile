package com.dev.lbsuhack.core.network.auth

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException

interface TokenPersistence {

    fun read(): ByteArray?

    fun write(bytes: ByteArray)
}

class AtomicFileTokenPersistence(
    context: Context,
) : TokenPersistence {

    private val file = AtomicFile(
        File(
            context.applicationContext.noBackupFilesDir,
            FILE_NAME,
        )
    )

    override fun read(): ByteArray? = try {
        file.openRead().use { it.readBytes() }
    } catch (_: FileNotFoundException) {
        null
    }

    override fun write(bytes: ByteArray) {
        val stream = file.startWrite()

        try {
            stream.write(bytes)
            file.finishWrite(stream)
        } catch (error: Throwable) {
            file.failWrite(stream)
            throw error
        }
    }

    private companion object {
        const val FILE_NAME = "auth_tokens.enc"
    }
}
