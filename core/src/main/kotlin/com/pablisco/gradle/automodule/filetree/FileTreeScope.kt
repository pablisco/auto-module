package com.pablisco.gradle.automodule.filetree

import com.pablisco.gradle.automodule.utils.createDirectories
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
interface FileTreeScope {
    /**
     * Creates a new folder with [this] as the name of the folder.
     *
     * @param block Used to continue deeper in the tree of files.
     */
    operator fun String.invoke(block: FileTreeScope.() -> Unit)

    fun folder(path: String, block: FileTreeScope.() -> Unit)
    /**
     * Creates an empty file with [this] as the path of the file (local or absolute)
     */
    operator fun String.invoke()

    @Suppress("MemberVisibilityCanBePrivate") // Api
    fun emptyFile(path: String)

    /**
     * Creates a file with the given content and [this] as the path of the file (local or absolute)
     *
     * @param this Location of the file relative to the current scope
     * @param content Text to be written in the file
     */
    @Suppress("KDocUnresolvedReference")
    infix operator fun String.plusAssign(content: String)

    /**
     * Creates a file with the given [content] and the [path] of the file (local or absolute)
     *
     * @param path Location of the file relative to the current scope
     * @param content Text to be written in the file
     */
    fun file(path: String, content: String)
}

private class DefaultFileTreeScope(private val path: Path) : FileTreeScope {

    override operator fun String.invoke(block: FileTreeScope.() -> Unit) {
        folder(this, block)
    }

    override fun folder(path: String, block: FileTreeScope.() -> Unit) {
        val branch = this.path.resolve(path)
        branch.createDirectories()
        DefaultFileTreeScope(branch).block()
    }

    override operator fun String.invoke() {
        emptyFile(this)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    override // Api
    fun emptyFile(path: String) {
        file(path, "")
    }

    @Suppress("KDocUnresolvedReference")
    override infix operator fun String.plusAssign(content: String) {
        file(this, content)
    }

    override fun file(path: String, content: String) {
        val finalPath = this.path.resolve(path)
        finalPath.parent.createDirectories()
        Files.write(finalPath, content.toByteArray())
    }

}

/**
 * Entry point for [FileTreeScope]
 */
internal fun Path.fileTree(block: FileTreeScope.() -> Unit) {
    DefaultFileTreeScope(this).block()
}