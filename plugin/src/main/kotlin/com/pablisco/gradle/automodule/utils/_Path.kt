package com.pablisco.gradle.automodule.utils

import java.io.InputStream
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import java.security.MessageDigest
import kotlin.streams.asSequence

internal fun Path.walk(): Sequence<Path> =
    sequence { yieldAll(Files.walk(this@walk).asSequence()) }

internal fun Path.list(): Sequence<Path> = when {
    Files.isDirectory(this) -> sequence { yieldAll(Files.list(this@list).asSequence()) }
    else -> emptySequence()
}

internal val Path.name: String
    get() = fileName.toString()

internal fun Path.exists(): Boolean = Files.exists(this)

internal fun Path.delete() = Files.delete(this)

internal fun Path.isDirectory(): Boolean = Files.isDirectory(this)

internal fun Path.deleteRecursively() {
    if (exists()) {
        walk().sortedDescending().forEach(Path::delete)
    }
}

internal val Path.inputStream: InputStream
    get() = Files.newInputStream(this)

internal fun Path.readText(): String? = Files.readAllLines(this).joinToString("\n")

internal fun Path.createFile(content: String): Path =
    apply { parent.createDirectories() }.ifExists { delete() }.createFile().write(content)

internal fun Path.ifExists(block: Path.() -> Unit): Path = apply { if (exists) block() }

internal val Path.exists get() = Files.exists(this)

internal fun Path.write(bytes: ByteArray, vararg openOption: OpenOption): Path =
    Files.write(this, bytes, *openOption)
internal fun Path.write(string: String, vararg openOption: OpenOption): Path =
    write(string.toByteArray(), *openOption)

internal fun Path.createFile(): Path = Files.createFile(this)

internal fun Path.createDirectories(
    vararg fileAttributes: FileAttribute<*> = emptyArray()
): Path = Files.createDirectories(this, *fileAttributes)

internal fun Path.maybeReadText(): String? =
    takeIf { it.exists() }?.readText()

internal fun Path.md5(): String = when {
    exists() -> inputStream.use {
        val md5 = MessageDigest.getInstance("MD5")
        BigInteger(md5.digest(it.readBytes()))
            .toString(16)

    }
    else -> ""
}.padStart(32, '0')

internal fun Path.toGradlePath(): String = joinToString(separator = ":", prefix = ":") { it.name }



