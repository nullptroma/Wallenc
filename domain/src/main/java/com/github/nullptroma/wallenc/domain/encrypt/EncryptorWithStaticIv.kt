package com.github.nullptroma.wallenc.domain.encrypt

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
import com.github.nullptroma.wallenc.domain.encrypt.Encryptor.Companion.AES_SETTINGS
import com.github.nullptroma.wallenc.domain.encrypt.Encryptor.Companion.IV_LEN
import kotlinx.coroutines.DisposableHandle
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class EncryptorWithStaticIv(private var secretKey: SecretKey, iv: ByteArray) : DisposableHandle {
    private val ivSpec = IvParameterSpec(iv)

    @OptIn(ExperimentalEncodingApi::class)
    fun encryptString(str: String): String {
        val bytesToEncrypt = str.toByteArray(Charsets.UTF_8)
        val encryptedBytes = encryptBytes(bytesToEncrypt)
        return Base64.Default.encode(encryptedBytes).replace("/", ".")
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decryptString(str: String): String {
        val bytesToDecrypt = Base64.Default.decode(str.replace(".", "/"))
        val decryptedBytes = decryptBytes(bytesToDecrypt)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun encryptBytes(bytes: ByteArray): ByteArray {
        if(secretKey.isDestroyed)
            throw Exception("Object was destroyed")
        val cipher = Cipher.getInstance(AES_SETTINGS)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = cipher.doFinal(bytes) // зашифрованные байты
        return encryptedBytes
    }

    fun decryptBytes(bytes: ByteArray): ByteArray {
        if(secretKey.isDestroyed)
            throw Exception("Object was destroyed")
        val cipher = Cipher.getInstance(AES_SETTINGS)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decryptedBytes = cipher.doFinal(bytes)
        return decryptedBytes
    }

    fun encryptStream(stream: OutputStream): OutputStream {
        if(secretKey.isDestroyed)
            throw Exception("Object was destroyed")
        val cipher = Cipher.getInstance(AES_SETTINGS)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec) // инициализация шифратора
        return CipherOutputStream(stream, cipher)
    }

    fun decryptStream(stream: InputStream): InputStream {
        if(secretKey.isDestroyed)
            throw Exception("Object was destroyed")
        val cipher = Cipher.getInstance(AES_SETTINGS)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return CipherInputStream(stream, cipher)
    }

    override fun dispose() {
        secretKey.destroy()
    }
}