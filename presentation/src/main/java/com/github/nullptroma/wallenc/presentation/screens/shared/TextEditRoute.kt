package com.github.nullptroma.wallenc.presentation.screens.shared

import com.github.nullptroma.wallenc.presentation.screens.ScreenRoute
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class TextEditRoute(val text: String): ScreenRoute()
