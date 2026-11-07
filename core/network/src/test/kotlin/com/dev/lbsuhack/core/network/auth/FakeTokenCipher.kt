package com.dev.lbsuhack.core.network.auth

class FakeTokenCipher : TokenCipher {

    var failDecrypt = false

    override fun encrypt(plaintext: ByteArray): ByteArray = plaintext.copyOf()

    override fun decrypt(payload: ByteArray): ByteArray {
        if (failDecrypt) {
            throw java.security.KeyStoreException("Token encryption key is missing")
        }
        return payload.copyOf()
    }
}
