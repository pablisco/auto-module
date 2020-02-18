package com.pablisco.gradle.automodule

import java.io.File

internal data class ModuleNode(
    val name: String,
    val path: String?,
    val children: List<ModuleNode> = emptyList()
)

internal fun File.lookupModules(): Sequence<ModuleNode> =
    walkTopDown()
        .filter { it.isGroovyBuildScript() or it.isKotlinBuildScript() }
        .map { file -> file.parentFile }
        .filterNot { dir -> dir.startsWith(".") }
        .filterNot { dir -> dir.endsWith("buildSrc") }
        .filterNot { dir -> dir == this }
        .map { it.path }
        .map { it.removePrefix(path) }
        .map { it.replace(File.separatorChar, ':') }
        .map { it.toNode() }

private fun File.isGroovyBuildScript() = name == "build.gradle"
private fun File.isKotlinBuildScript() = name == "build.gradle.kts"

private fun String.toNode(): ModuleNode =
    split(":").filterNot(String::isBlank).toNode()

private fun List<String>.toNode(path: String? = null): ModuleNode {
    val name = first()
    val newPath = (path ?: "") + ":" + name
    return when {
        isEmpty() -> error("oops")
        size == 1 -> ModuleNode(name, path = newPath)
        else -> {
            ModuleNode(
                name = name,
                path = newPath,
                children = listOf(drop(1).toNode(newPath))
            )
        }
    }
}

internal val Collection<ModuleNode>.allModules: Collection<ModuleNode>
    get() = flatMap { listOf(it) + it.children }

internal fun Collection<ModuleNode>.cleanup(): Collection<ModuleNode> =
    groupingBy { node -> node.name }
        .aggregate { _, acc: ModuleNode?, node, _ ->
            acc?.run { copy(children = children + node.children) } ?: node
        }.values

