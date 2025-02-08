package com.github.nullptroma.wallenc.domain.datatypes

data class StorageEncryptionInfo(
    val encryptedTestData: String,
    val pathIv: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StorageEncryptionInfo

        if (encryptedTestData != other.encryptedTestData) return false
        if (!pathIv.contentEquals(other.pathIv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encryptedTestData.hashCode()
        result = 31 * result + pathIv.contentHashCode()
        return result
    }
}