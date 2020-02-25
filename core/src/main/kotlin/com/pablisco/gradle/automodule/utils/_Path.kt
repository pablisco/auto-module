package com.pablisco.gradle.automodule.utils

import java.nio.file.Files
import java.nio.file.Path
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

@Suppress("PlatformExtensionReceiverOfInline")
internal fun Path.readText(): String? = Files.readAllLines(this).joinToString("\n")

internal fun Path.createFile(content: String) {
    Files.createDirectories(parent)
    if (Files.exists(this)) {
        Files.delete(this)
    }
    Files.createFile(this)
    Files.write(this, content.toByteArray())
}
