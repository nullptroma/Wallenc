package com.github.nullptroma.wallenc.presentation.utils


fun debouncedLambda(debounceMs: Long = 300, action: ()->Unit) : ()->Unit {
    var latest: Long = 0
    return {
        val now = System.currentTimeMillis()
        if (now - latest >= debounceMs) {
            latest = now
            action()
        }
    }
}