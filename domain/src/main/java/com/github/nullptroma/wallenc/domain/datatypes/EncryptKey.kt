package com.github.nullptroma.wallenc.domain.datatypes

import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

class EncryptKey {
    val bytes: ByteArray

    constructor(password: String) {
        val digest = MessageDigest.getInstance("SHA-256")
        bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
    }

    constructor(key: ByteArray) {
        this.bytes = key.clone()
    }

    fun toAesKey() : SecretKeySpec {
        return SecretKeySpec(bytes, "AES")
    }
}