package com.github.nullptroma.wallenc.presentation.extensions

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp

fun Modifier.ignoreHorizontalParentPadding(horizontal: Dp): Modifier {
    return this.layout { measurable, constraints ->
        val overrideWidth = constraints.maxWidth + 2 * horizontal.roundToPx()
        val placeable = measurable.measure(constraints.copy(maxWidth = overrideWidth))
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

fun Modifier.ignoreVerticalParentPadding(vertical: Dp): Modifier {
    return this.layout { measurable, constraints ->
        val overrideHeight = constraints.maxHeight + 2 * vertical.roundToPx()
        val placeable = measurable.measure(constraints.copy(maxHeight = overrideHeight))
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}
