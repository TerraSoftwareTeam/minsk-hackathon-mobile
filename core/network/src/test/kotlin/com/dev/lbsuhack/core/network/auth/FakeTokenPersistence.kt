package com.dev.lbsuhack.core.network.auth

class FakeTokenPersistence(
    stored: ByteArray? = null,
) : TokenPersistence {

    var stored: ByteArray? = stored
        private set

    var readCount = 0
        private set

    var writeCount = 0
        private set

    override fun read(): ByteArray? {
        readCount += 1
        return stored?.copyOf()
    }

    override fun write(bytes: ByteArray) {
        writeCount += 1
        stored = bytes.copyOf()
    }
}
