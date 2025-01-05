package com.github.nullptroma.wallenc.domain.datatypes

import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

class EncryptKey(val key: String) {
    fun to32Bytes(): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(key.toByteArray(Charsets.UTF_8))
    }

    fun toAesKey() : SecretKeySpec {
        return SecretKeySpec(to32Bytes(), "AES")
    }
}