package com.github.secretx33.whisperjsontosrt.compress

import com.github.secretx33.whisperjsontosrt.isLazyInitialized
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.InputStream
import java.nio.file.Path

class ZipCompressedFile(override val compressedFile: Path) : ApacheCompressedFile<ZipArchiveEntry>() {

    private val file by lazy(LazyThreadSafetyMode.NONE) { ZipFile(compressedFile.toFile()) }

    override fun listFiles(): Sequence<ZipArchiveEntry> = file.entries.asSequence().filter { !it.isDirectory }

    override fun getFileInputStream(entry: ZipArchiveEntry): InputStream = file.getInputStream(entry)

    override fun close() {
        if (!::file.isLazyInitialized) return
        file.close()
    }

}