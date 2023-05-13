package com.github.secretx33.whisperjsontosrt

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

fun processFiles(args: Array<String>): Collection<Path> {
    val operations = listOf(
        ::handleFolders,
        ::validatePaths,
    )
    return operations.fold(args.mapTo(mutableSetOf(), ::Path) as Collection<Path>) { files, operation ->
        operation(files)
    }
}

private fun handleFolders(paths: Collection<Path>): Collection<Path> = try {
    paths.flatMapTo(mutableSetOf()) { file ->
        when {
            file.isDirectory() -> Files.walk(file, 5).use { folderContent ->
                folderContent.filter { it.isRegularFile() && it.isSupportedFormat() }.toList()
            }
            else -> listOf(file)
        }
    }
} catch (e: Exception) {
    exitWithMessage("Error: could not parse provided paths '$paths'.\n${e.stackTraceToString()}")
}

private fun validatePaths(paths: Collection<Path>): Collection<Path> {
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

private fun Path.isSupportedFormat(): Boolean = extension.equals("json", ignoreCase = true)