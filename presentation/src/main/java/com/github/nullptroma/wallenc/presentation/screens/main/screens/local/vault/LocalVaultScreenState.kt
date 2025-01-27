package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import com.github.nullptroma.wallenc.domain.datatypes.Tree
import com.github.nullptroma.wallenc.domain.interfaces.IStorageInfo

data class LocalVaultScreenState(val storagesList: List<Tree<IStorageInfo>>)