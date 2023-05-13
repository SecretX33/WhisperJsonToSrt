package com.github.secretx33.whisperjsontosrt

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.secretx33.whisperjsontosrt.exception.QuitApplicationException
import java.io.InputStream
import java.nio.file.Path
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.outputStream
import kotlin.time.Duration.Companion.seconds

val jackson: ObjectMapper by lazy(LazyThreadSafetyMode.NONE) {
    ObjectMapper().findAndRegisterModules().apply {
        setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
}

fun Path.createFileIfNotExists(): Path {
    if (exists()) return this
    parent?.createDirectories()
    return createFile()
}

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss,SSS", Locale.US)

fun Double.formatSeconds(): String {
    val time = LocalTime.ofNanoOfDay(seconds.inWholeNanoseconds)
    return formatter.format(time)
}

fun printError(message: String) = System.err.println(message)

fun exitWithMessage(message: String): Nothing = throw QuitApplicationException(message)

fun InputStream.copyTo(file: Path) {
    require(!file.isDirectory()) { "file must not be a directory, file = $file" }
    file.createFileIfNotExists()
    buffered().use { inputStream ->
        file.outputStream().buffered().use(inputStream::copyTo)
    }
}

fun Path.deleteIfExistsSilently() = try {
    deleteIfExists()
} catch (_: Exception) {
}

@OptIn(ExperimentalPathApi::class)
fun Path.deleteRecursivelySilently() = try {
    deleteRecursively()
    deleteIfExists()
} catch (_: Exception) {
}