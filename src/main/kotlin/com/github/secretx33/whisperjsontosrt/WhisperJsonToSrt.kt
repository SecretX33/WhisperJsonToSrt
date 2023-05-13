@file:OptIn(ExperimentalTime::class)

package com.github.secretx33.whisperjsontosrt

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.secretx33.whisperjsontosrt.exception.QuitApplicationException
import com.github.secretx33.whisperjsontosrt.model.AppResources
import com.github.secretx33.whisperjsontosrt.model.WhisperJson
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun main(args: Array<String>) {
    try {
        bootstrapApplication(args)
    } catch (t: Throwable) {
        when (t) {
            is QuitApplicationException -> t.message?.let(::printError)
            else -> printError("Error: ${t::class.simpleName}: ${t.message}\n${t.stackTraceToString()}")
        }
    }
}

private fun bootstrapApplication(args: Array<String>) = AppResources().use { it.processArguments(args) }

private fun AppResources.processArguments(args: Array<String>) {
    val (subtitleFiles, duration) = measureTimedValue {
        processFiles(args).onEach(::convertJsonToSrt)
    }
    println("Successfully converted ${subtitleFiles.size} .json file(s) into .srt in ${duration.inWholeMilliseconds}ms.")
}

private fun convertJsonToSrt(file: Path) {
    val fileContent = try {
        file.readText()
    } catch (e: Exception) {
        exitWithMessage("Error: failed to read file '${file.name}'.\n${e.stackTraceToString()}")
    }

    val whisperJson = try {
        jackson.readValue<WhisperJson>(fileContent)
    } catch (e: Exception) {
        exitWithMessage("Error: failed to parse file '${file.name}'. Maybe there is some syntax error in the file.\n${e.stackTraceToString()}")
    }

    val convertedSubtitle = whisperJson.segments.mapIndexed { index, subtitleEntry ->
        """
            ${index + 1}
            ${subtitleEntry.start.formatSeconds()} --> ${subtitleEntry.end.formatSeconds()}
            ${subtitleEntry.text.trim()}
        """.trimIndent()
    }.joinToString("\n\n", postfix = "\n")

    val convertedFile = (file.parent ?: Path("")) / "${file.nameWithoutExtension}.srt"
    try {
        convertedFile.createFileIfNotExists()
            .writeText(convertedSubtitle)
    } catch (e: Exception) {
        exitWithMessage("Error: failed to write file '${convertedFile.name}' to disk.\n${e.stackTraceToString()}")
    }
}
