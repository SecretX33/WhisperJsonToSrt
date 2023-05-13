package com.github.secretx33.whisperjsontosrt.compress

import java.io.InputStream
import java.nio.file.Path

sealed interface CompressedFile<T> : AutoCloseable {

    val compressedFile: Path

    /**
     * Return only files, not directories.
     */
    fun listFiles(): Sequence<T>

    fun getFileInputStream(entry: T): InputStream

    fun getEntryName(entry: T): String

}