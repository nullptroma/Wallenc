package com.github.nullptroma.wallenc.app

import com.github.nullptroma.wallenc.domain.interfaces.ILogger
import timber.log.Timber

class Logger: ILogger {
    override fun debug(tag: String, msg: String) {
        Timber.tag(tag)
        Timber.d(msg)
    }
}