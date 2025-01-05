package com.github.nullptroma.wallenc.domain

import com.github.nullptroma.wallenc.domain.datatypes.EncryptKey
import com.github.nullptroma.wallenc.domain.encrypt.Encryptor
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Random

class EncryptorTest {
    val key1 = EncryptKey("key1")
    val key2 = EncryptKey("key2")
    val rnd = Random()

    @Test
    fun `test correct key for StorageEncryptionInfo`() {
        val encInfo = Encryptor.generateEncryptionInfo(key1)
        val res = Encryptor.checkKey(key = key1, encInfo = encInfo)
        assertEquals(true, res)
    }

    @Test
    fun `test incorrect key for StorageEncryptionInfo`() {
        val encInfo = Encryptor.generateEncryptionInfo(key1)
        val res = Encryptor.checkKey(key = key2, encInfo = encInfo)
        assertEquals(false, res)
    }

    @Test
    fun `test string encryption with the same key`() {
        val text = "Hello world, my name is Wallenc!"
        val encryptor = Encryptor(key1.toAesKey())
        val encryptedText = encryptor.encryptString(text)

        val newEncryptor = Encryptor(key1.toAesKey())
        val decryptedText = newEncryptor.decryptString(encryptedText)

        assertEquals(text, decryptedText)
    }

    @Test
    fun `test string encryption with the wrong key`() {
        val text = "Hello world, my name is Wallenc!"
        val encryptor = Encryptor(key1.toAesKey())
        val encryptedText = encryptor.encryptString(text)

        val newEncryptor = Encryptor(key2.toAesKey())
        try {
            val decryptedText = newEncryptor.decryptString(encryptedText)
            fail("There is not exception on decrypt with wrong key")
        }
        catch (e: Exception) {
            // good
        }
    }

    @Test
    fun `test bytes encryption with the same key`() {
        val bytes = ByteArray(512)
        rnd.nextBytes(bytes)
        val encryptor = Encryptor(key1.toAesKey())
        val encryptedBytes = encryptor.encryptBytes(bytes)

        val newEncryptor = Encryptor(key1.toAesKey())
        val decryptedBytes = newEncryptor.decryptBytes(encryptedBytes)

        assertThat(bytes, IsNot.not(IsEqual.equalTo(encryptedBytes)))
        assertThat(bytes, Is.`is`(IsEqual.equalTo(decryptedBytes)))
    }

    @Test
    fun `test bytes encryption with the wrong key`() {
        val bytes = ByteArray(512)
        rnd.nextBytes(bytes)
        val encryptor = Encryptor(key1.toAesKey())
        val encryptedBytes = encryptor.encryptBytes(bytes)

        val newEncryptor = Encryptor(key2.toAesKey())
        try {
            val decryptedBytes = newEncryptor.decryptBytes(encryptedBytes)
            fail("There is not exception on decrypt with wrong key")
        }
        catch (e: Exception) {
            // good
        }
    }

    @Test
    fun `test stream encryption with the same key`() {
        val dataLen = 1500
        val origData = ByteArray(dataLen)
        rnd.nextBytes(origData)
        val encryptor = Encryptor(key1.toAesKey())

        val streamForEncrypt = ByteArrayOutputStream(dataLen*3)
        val encryptedStream = encryptor.encryptStream(streamForEncrypt)
        encryptedStream.write(origData)
        encryptedStream.close()
        val encryptedData = streamForEncrypt.toByteArray()

        val newEncryptor = Encryptor(key1.toAesKey())
        val streamForDecrypt = ByteArrayInputStream(encryptedData)
        val decryptedStream = newEncryptor.decryptStream(streamForDecrypt)
        val decryptedData = decryptedStream.readAllBytes()

        assertArrayEquals(origData, decryptedData)
    }

    @Test
    fun `test stream encryption with the wrong key`() {
        val dataLen = 1500
        val origData = ByteArray(dataLen)
        rnd.nextBytes(origData)
        val encryptor = Encryptor(key1.toAesKey())

        val streamForEncrypt = ByteArrayOutputStream(dataLen*3)
        val encryptedStream = encryptor.encryptStream(streamForEncrypt)
        encryptedStream.write(origData)
        encryptedStream.close()
        val encryptedData = streamForEncrypt.toByteArray()

        val newEncryptor = Encryptor(key2.toAesKey())
        try {
            val streamForDecrypt = ByteArrayInputStream(encryptedData)
            val decryptedStream = newEncryptor.decryptStream(streamForDecrypt)
            val decryptedData = decryptedStream.readAllBytes()
            fail("There is not exception on decrypt with wrong key")
        }
        catch (e: Exception) {
            // good
        }
    }
}