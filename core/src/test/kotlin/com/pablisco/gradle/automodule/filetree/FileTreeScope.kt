package com.pablisco.gradle.automodule.filetree

import java.nio.file.Files
import java.nio.file.Path

internal class FileTreeScope(private val path: Path) {

    operator fun String.invoke(block: FileTreeScope.() -> Unit) {
        val branch = path.resolve(this)
        Files.createDirectories(branch)
        FileTreeScope(branch).block()
    }

    operator fun String.invoke() {
        this += ""
    }

    infix operator fun String.plusAssign(content: String) {
        Files.write(path.resolve(this), content.toByteArray())
    }

}

internal fun Path.fileTree(block: FileTreeScope.() -> Unit) {
    FileTreeScope(this).block()
}