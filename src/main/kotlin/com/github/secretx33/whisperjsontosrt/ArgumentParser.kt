@file:OptIn(ExperimentalPathApi::class)

package com.github.secretx33.whisperjsontosrt

import com.github.secretx33.whisperjsontosrt.model.AppResources
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

fun AppResources.processFiles(args: Array<String>): Set<Path> {
    val operations = listOf(
        ::handleFolders,
        ::extractCompressedFiles,
        ::validatePaths,
    )
    val processedFiles = args.mapTo(mutableSetOf(), ::Path) as Set<Path>
    return operations.fold(processedFiles) { files, operation ->
        operation(files)
    }
}

private fun handleFolders(files: Set<Path>): Set<Path> = try {
    files.flatMapTo(mutableSetOf()) { file ->
        when {
            file.isDirectory() -> Files.walk(file, 5).use { folderContent ->
                folderContent.filter { it.isRegularFile() && it.isSupportedFormat() }.toList()
            }
            else -> listOf(file)
        }
    }
} catch (e: Exception) {
    exitWithMessage("Error: could not parse provided paths '$files'.\n${e.stackTraceToString()}")
}

private val supportedExtensions = setOf("json")

private fun Path.isSupportedFormat(): Boolean = supportedExtensions.any { extension.equals(it, ignoreCase = true) }

private fun AppResources.extractCompressedFiles(files: Set<Path>): Set<Path> = files.flatMapTo(mutableSetOf()) {
    when (it.extension.lowercase()) {
        "7z" -> extractSevenZipFile(it)
        else -> setOf(it)
    }
}

private fun AppResources.extractSevenZipFile(path: Path): Set<Path> {
    val temporaryFolder = createTempDirectory(UUID.randomUUID().toString().replace("-", "")).registerForRemoval()
    val destinyFolder = path.parent?.resolve(path.nameWithoutExtension) ?: Path(path.nameWithoutExtension)

    if (destinyFolder.isRegularFile() || destinyFolder.walk().drop(1).any()) {
        // If there is a file with the name of the 7z, or a folder with at least one file inside, don't do anything
        return emptySet()
    }

    try {
        SevenZFile(path.toFile()).use {
            var entry = it.nextEntry
            while (entry?.isDirectory == true) {
                entry = it.nextEntry
            }

            while (entry != null) {
                val entryFile = temporaryFolder / entry.name

                it.getInputStream(entry).copyTo(entryFile)

                do {
                    entry = it.nextEntry
                } while (entry?.isDirectory == true)
            }
        }

        val hasOnlyOneFolder = temporaryFolder.listDirectoryEntries().let { it.size == 1 && it.first().isDirectory() }
        val referenceFolderToRelativize = if (!hasOnlyOneFolder) {
            temporaryFolder
        } else {
            temporaryFolder.listDirectoryEntries().first()
        }

        temporaryFolder.walk().filter { it.isRegularFile() && it.isSupportedFormat() }.forEach {
            val relativePath = it.relativeTo(referenceFolderToRelativize)
            val destinationFile = destinyFolder / relativePath
            destinationFile.parent?.createDirectories()
            it.moveTo(destinationFile)
        }

        return destinyFolder.walk()
            .onEach { it.registerForRemoval() }
            .filterTo(mutableSetOf()) { it.isRegularFile() && it.isSupportedFormat() }
            .toSet()
    } catch (e: Exception) {
        destinyFolder.registerForRemoval()
        exitWithMessage("Error: failed to extract compressed file '$path'.\n${e.stackTraceToString()}")
    }
}

private fun validatePaths(paths: Set<Path>): Set<Path> {
    var errorMessage: String? = null

    if (paths.isEmpty()) {
        errorMessage = "Invalid argument: this program requires at least one file passed as argument to function."
    }
    paths.onEach {
        errorMessage = errorMessage ?: when {
            it.notExists() -> "Invalid argument: file '$it' does not seems to exist."
            !it.isSupportedFormat() -> "Invalid argument: file '$it' is of an illegal type '${it.extension}', this program only support .json files."
            else -> null
        }
    }
    errorMessage?.let(::exitWithMessage)
    return paths
}