package com.pablisco.gradle.automodule.filetree

import java.nio.file.Files
import java.nio.file.Path

/**
 * Used to generate a tree of files and directories.
 *
 * Inside this scope we can define file structures:
 *
 * ```
 * path.fileTree {
 *    "parent" { // creates: "$path/parent/"
 *        "child" { // creates: "$path/parent/child/"
 *            "file.txt"() // creates: "$path/parent/child/file.txt"
 *            "file2.txt" += "file content" // creates: "$path/parent/child/file2.txt" with content
 *        }
 *    }
 * }
 * ```
 *
 * In a similar fashion the same can be done using a flat definition:
 * ```
 * path.fileTree {
 *    // creates: "$path/parent/child/file.txt"
 *    "parent/child/file.txt"()
 *    // creates: "$path/parent/child/file2.txt" with content
 *    "parent/child/file2.txt" += "file content"
 * }
 * ```
 *
 */
class FileTreeScope(private val path: Path) {

    /**
     * Creates a new folder with [this] as the name of the folder.
     *
     * @param block Used to continue deeper in the tree of files.
     */
    operator fun String.invoke(block: FileTreeScope.() -> Unit) {
        val branch = path.resolve(this)
        Files.createDirectories(branch)
        FileTreeScope(branch).block()
    }

    /**
     * Creates an empty file with [this] as the path of the file (local or absolute)
     */
    operator fun String.invoke() {
        this += ""
    }

    /**
     * Creates a file with the given content and [this] as the path of the file (local or absolute)
     */
    infix operator fun String.plusAssign(content: String) {
        Files.write(path.resolve(this), content.toByteArray())
    }

}

/**
 * Entry point for [FileTreeScope]
 */
internal fun Path.fileTree(block: FileTreeScope.() -> Unit) {
    FileTreeScope(this).block()
}