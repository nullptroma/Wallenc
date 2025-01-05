package com.github.nullptroma.wallenc.domain.encrypt

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

class Encryptor(private var _secretKey: SecretKey?) : DisposableHandle {
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

    private fun encryptBytes(bytes: ByteArray): ByteArray {
        val secretKey = _secretKey ?: throw Exception("Object was disposed")
        val cipher = Cipher.getInstance(AES_SETTINGS)
        val iv = IvParameterSpec(Random.nextBytes(IV_LEN))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        val encryptedBytes = iv.iv + cipher.doFinal(bytes) // iv + зашифрованные байты
        return encryptedBytes
    }

    private fun decryptBytes(bytes: ByteArray): ByteArray {
        val secretKey = _secretKey ?: throw Exception("Object was disposed")
        val cipher = Cipher.getInstance(AES_SETTINGS)
        val iv = IvParameterSpec(bytes.take(IV_LEN).toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        val decryptedBytes = cipher.doFinal(bytes.drop(IV_LEN).toByteArray())
        return decryptedBytes
    }

    fun encryptStream(stream: OutputStream): OutputStream {
        val secretKey = _secretKey ?: throw Exception("Object was disposed")
        val iv = IvParameterSpec(Random.nextBytes(IV_LEN))
        stream.write(iv.iv) // Запись инициализационного вектора сырой файл
        val cipher = Cipher.getInstance(AES_SETTINGS)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv) // инициализация шифратора
        return CipherOutputStream(stream, cipher)
    }

    fun decryptStream(stream: InputStream): InputStream {
        val secretKey = _secretKey ?: throw Exception("Object was disposed")
        val ivBytes = ByteArray(IV_LEN) // Буфер для 16 байт IV
        val bytesRead = stream.read(ivBytes) // Чтение IV вектора
        if(bytesRead != IV_LEN)
            throw Exception("TODO iv не прочитан")
        val iv = IvParameterSpec(ivBytes)

        val cipher = Cipher.getInstance(AES_SETTINGS)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return CipherInputStream(stream, cipher)
    }

    override fun dispose() {
        _secretKey?.destroy()
        _secretKey = null
    }

    companion object {
        private const val IV_LEN = 16
        private const val AES_SETTINGS = "AES/CBC/PKCS5Padding"

        
    }
}