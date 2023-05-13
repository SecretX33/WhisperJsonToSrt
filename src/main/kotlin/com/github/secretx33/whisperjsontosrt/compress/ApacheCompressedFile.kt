package com.github.secretx33.whisperjsontosrt.compress

import org.apache.commons.compress.archivers.ArchiveEntry

sealed class ApacheCompressedFile<T : ArchiveEntry> : AbstractCompressedFile<T>() {

    override fun isDirectory(entry: T?): Boolean = entry?.isDirectory == true

    override fun getEntryName(entry: T): String = entry.name

}