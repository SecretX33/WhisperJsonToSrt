package com.github.secretx33.whisperjsontosrt.compress

sealed class AbstractCompressedFile<T> : CompressedFile<T> {

    protected abstract fun isDirectory(entry: T?): Boolean

    protected fun archiveEntrySequence(
        getNextEntry: () -> T?,
    ) : Sequence<T> = sequence {
        var entry: T?
        do {
            entry = getNextEntry()
        } while (isDirectory(entry))

        while (entry != null) {
            yield(entry)

            do {
                entry = getNextEntry()
            } while (isDirectory(entry))
        }
    }

}