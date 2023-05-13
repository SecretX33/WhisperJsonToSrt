package com.github.secretx33.whisperjsontosrt.compress

import com.github.secretx33.whisperjsontosrt.isLazyInitialized
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.InputStream
import java.nio.file.Path

class SevenZipCompressedFile(override val compressedFile: Path) : ApacheCompressedFile<SevenZArchiveEntry>() {

    private val file by lazy(LazyThreadSafetyMode.NONE) { SevenZFile(compressedFile.toFile()) }

    override fun listFiles(): Sequence<SevenZArchiveEntry> = archiveEntrySequence { file.nextEntry }

    override fun getFileInputStream(entry: SevenZArchiveEntry): InputStream = file.getInputStream(entry)

    override fun close() {
        if (!::file.isLazyInitialized) return
        file.close()
    }

}