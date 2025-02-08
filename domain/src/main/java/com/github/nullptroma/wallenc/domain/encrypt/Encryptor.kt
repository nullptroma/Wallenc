package com.github.nullptroma.wallenc.domain.encrypt

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.datatypes.StorageEncryptionInfo
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

class Encryptor(private var secretKey: SecretKey) : DisposableHandle {
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
        val ivSpec = IvParameterSpec(Random.nextBytes(IV_LEN))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = ivSpec.iv + cipher.doFinal(bytes) // iv + зашифрованные байты
        return encryptedBytes
    }

    fun decryptBytes(bytes: ByteArray): ByteArray {
        if(secretKey.isDestroyed)
            throw Exception("Object was destroyed")
        val cipher = Cipher.getInstance(AES_SETTINGS)
        val ivSpec = IvParameterSpec(bytes.take(IV_LEN).toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decryptedBytes = cipher.doFinal(bytes.drop(IV_LEN).toByteArray())
        return decryptedBytes
    }

    fun encryptStream(stream: OutputStream): OutputStream {
        if(secretKey.isDestroyed)
            throw Exception("Object was destroyed")
        val ivSpec = IvParameterSpec(Random.nextBytes(IV_LEN))
        stream.write(ivSpec.iv) // Запись инициализационного вектора сырой файл
        val cipher = Cipher.getInstance(AES_SETTINGS)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec) // инициализация шифратора
        return CipherOutputStream(stream, cipher)
    }

    fun decryptStream(stream: InputStream): InputStream {
        if(secretKey.isDestroyed)
            throw Exception("Object was destroyed")
        val ivBytes = ByteArray(IV_LEN) // Буфер для 16 байт IV
        val bytesRead = stream.read(ivBytes) // Чтение IV вектора
        if(bytesRead != IV_LEN)
            throw Exception("TODO iv не прочитан")
        val ivSpec = IvParameterSpec(ivBytes)

        val cipher = Cipher.getInstance(AES_SETTINGS)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return CipherInputStream(stream, cipher)
    }

    override fun dispose() {
        //secretKey.destroy()
    }

    companion object {
        public const val IV_LEN = 16
        public const val AES_SETTINGS = "AES/CBC/PKCS5Padding"
        private const val TEST_DATA_LEN = 512

        @OptIn(ExperimentalEncodingApi::class)
        fun generateEncryptionInfo(key: EncryptKey, encryptPath: Boolean) : StorageEncryptionInfo {
            val encryptor = Encryptor(key.toAesKey())
            val testData = ByteArray(TEST_DATA_LEN)
            val encryptedData = encryptor.encryptBytes(testData)
            return StorageEncryptionInfo(
                encryptedTestData = Base64.Default.encode(encryptedData),
                pathIv = if(encryptPath) Random.nextBytes(IV_LEN) else null
            )
        }

        @OptIn(ExperimentalEncodingApi::class)
        fun checkKey(key: EncryptKey, encInfo: StorageEncryptionInfo): Boolean {
            val encryptor = Encryptor(key.toAesKey())
            try {
                val encData = Base64.Default.decode(encInfo.encryptedTestData)
                val testData = encryptor.decryptBytes(encData)
                return testData.all { it == 0.toByte() } && testData.size == TEST_DATA_LEN
            }
            catch (e: Exception) {
                return false
            }
        }
    }
}