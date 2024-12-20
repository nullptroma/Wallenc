package com.github.nullptroma.wallenc.data.vaults

interface IStorageCallbackHandler {
    fun changeSize(delta: Int)
    fun changeNumOfFiles(delta: Int)

}