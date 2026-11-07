package com.dev.lbsuhack.core.network.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface TokenCipher {

    fun encrypt(plaintext: ByteArray): ByteArray

    fun decrypt(payload: ByteArray): ByteArray
}

internal class KeystoreTokenCipher : TokenCipher {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
    }

    override fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        cipher.updateAAD(AAD)

        val ciphertext = cipher.doFinal(plaintext)
        val iv = cipher.iv

        check(iv.size == IV_SIZE)

        return byteArrayOf(FORMAT_VERSION) + iv + ciphertext
    }

    override fun decrypt(payload: ByteArray): ByteArray {
        require(payload.size >= 1 + IV_SIZE + TAG_SIZE_BYTES) {
            "Invalid encrypted token payload"
        }
        require(payload[0] == FORMAT_VERSION) {
            "Unsupported encrypted token format"
        }

        val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            ?: throw java.security.KeyStoreException(
                "Token encryption key is missing"
            )

        val iv = payload.copyOfRange(1, 1 + IV_SIZE)
        val ciphertext = payload.copyOfRange(
            1 + IV_SIZE,
            payload.size,
        )

        return Cipher.getInstance(TRANSFORMATION).run {
            init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(TAG_SIZE_BITS, iv),
            )
            updateAAD(AAD)
            doFinal(ciphertext)
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER,
        ).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or
                            KeyProperties.PURPOSE_DECRYPT,
                )
                    .setKeySize(256)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_NONE
                    )
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )

            generateKey()
        }
    }

    private companion object {
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "0504"
        const val TRANSFORMATION = "AES/GCM/NoPadding"

        const val FORMAT_VERSION: Byte = 1
        const val IV_SIZE = 12
        const val TAG_SIZE_BITS = 128
        const val TAG_SIZE_BYTES = TAG_SIZE_BITS / 8

        val AAD = "0504".toByteArray(Charsets.UTF_8)
    }
}
