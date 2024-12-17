package com.github.nullptroma.wallenc.domain.models

import kotlinx.coroutines.flow.StateFlow
import java.net.URL

interface IStorageExplorer {
    val currentPath: StateFlow<URL>

    // TODO
    // пока бесполезный интерфейс
}