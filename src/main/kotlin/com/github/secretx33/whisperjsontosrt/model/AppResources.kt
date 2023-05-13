package com.github.secretx33.whisperjsontosrt.model

import com.github.secretx33.whisperjsontosrt.deleteRecursivelySilently
import java.nio.file.Path

class AppResources : AutoCloseable {
    private val temporaryFiles: MutableSet<Path> = mutableSetOf()

    fun Path.registerForRemoval(): Path = apply(temporaryFiles::add)

    override fun close() = temporaryFiles.forEach { it.deleteRecursivelySilently() }
}
