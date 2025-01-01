package com.github.nullptroma.wallenc.domain.datatypes

import java.security.MessageDigest

class EncryptKey(val key: String) {
    fun to32Bytes(): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(key.toByteArray(Charsets.UTF_8))
    }
}