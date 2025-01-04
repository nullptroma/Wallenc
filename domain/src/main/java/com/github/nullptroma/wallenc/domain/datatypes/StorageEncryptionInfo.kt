package com.github.nullptroma.wallenc.domain.datatypes

data class StorageEncryptionInfo(
    val isEncrypted: Boolean,
    val encryptedTestData: String?
)