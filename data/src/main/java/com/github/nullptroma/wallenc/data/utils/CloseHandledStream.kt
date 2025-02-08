package com.github.nullptroma.wallenc.data.utils

import java.io.InputStream
import java.io.OutputStream

private class CloseHandledOutputStream(
    private val stream: OutputStream,
    private val onClosing: () -> Unit,
    private val onClose: () -> Unit
) : OutputStream() {

    override fun write(b: Int) {
        stream.write(b)
    }

    override fun write(b: ByteArray) {
        stream.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        stream.write(b, off, len)
    }

    override fun flush() {
        stream.flush()
    }

    override fun close() {
        onClosing()
        try {
            stream.close()
        } finally {
            onClose()
        }
    }
}

private class CloseHandledInputStream(
    private val stream: InputStream,
    private val onClosing: () -> Unit,
    private val onClose: () -> Unit
) : InputStream() {

    override fun read(): Int {
        return stream.read()
    }

    override fun read(b: ByteArray): Int {
        return stream.read(b)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return stream.read(b, off, len)
    }

    override fun skip(n: Long): Long {
        return stream.skip(n)
    }

    override fun available(): Int {
        return stream.available()
    }

    override fun close() {
        onClosing()
        try {
            stream.close()
        } finally {
            onClose()
        }
    }

    override fun mark(readlimit: Int) {
        stream.mark(readlimit)
    }

    override fun reset() {
        stream.reset()
    }

    override fun markSupported(): Boolean {
        return stream.markSupported()
    }
}

class CloseHandledStreamExtension {
    companion object {
        fun OutputStream.onClosed(callback: ()->Unit): OutputStream {
            return CloseHandledOutputStream(this, {}, callback)
        }

        fun InputStream.onClosed(callback: ()->Unit): InputStream {
            return CloseHandledInputStream(this, {}, callback)
        }

        fun OutputStream.onClosing(callback: ()->Unit): OutputStream {
            return CloseHandledOutputStream(this, callback, {})
        }

        fun InputStream.onClosing(callback: ()->Unit): InputStream {
            return CloseHandledInputStream(this, callback, {})
        }
    }
}