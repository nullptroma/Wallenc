package com.github.nullptroma.wallenc.domain.interfaces

import kotlinx.coroutines.flow.StateFlow

interface IStorageExplorer {
    val currentPath: StateFlow<String>

    // TODO
    // пока бесполезный интерфейс
}