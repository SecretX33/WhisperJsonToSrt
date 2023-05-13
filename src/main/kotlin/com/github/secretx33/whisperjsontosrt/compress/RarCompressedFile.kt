package com.github.secretx33.whisperjsontosrt.compress

import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import com.github.secretx33.whisperjsontosrt.isLazyInitialized
import java.io.InputStream
import java.nio.file.Path

class RarCompressedFile(override val compressedFile: Path) : AbstractCompressedFile<FileHeader>() {

    private val file by lazy(LazyThreadSafetyMode.NONE) { Archive(compressedFile.toFile()) }

    override fun listFiles(): Sequence<FileHeader> = file.fileHeaders.asSequence().filter { !it.isDirectory }

    override fun getFileInputStream(entry: FileHeader): InputStream = file.getInputStream(entry)

    override fun isDirectory(entry: FileHeader?): Boolean = entry?.isDirectory == true

    override fun getEntryName(entry: FileHeader): String = entry.fileName

    override fun close() {
        if (!::file.isLazyInitialized) return
        file.close()
    }

}