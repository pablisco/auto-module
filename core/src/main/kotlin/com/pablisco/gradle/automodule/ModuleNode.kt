package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.list
import com.pablisco.gradle.automodule.utils.name
import com.pablisco.gradle.automodule.utils.walk
import java.nio.file.Files
import java.nio.file.Path

internal data class ModuleNode(
    val name: String,
    val path: String?,
    val children: Sequence<ModuleNode>
)

internal fun Path.rootModule(
    ignored: List<String>,
    name: String
) = ModuleNode(
    name = name,
    path = null,
    children = findChildModules(
        ignored = ignored,
        scriptPaths = findScripts()
    )
)

internal fun Path.findScripts(isValidScript: Path.() -> Boolean = Path::isValidScript) =
    walk().filter { it.isValidScript() }.toList()

internal fun Path.findChildModules(
    ignored: List<String>,
    scriptPaths: List<Path>,
    rootPath: Path = this
): Sequence<ModuleNode> = list()
    .filterNot { it.name == "buildSrc" }
    .filterNot { it.name.startsWith(".") }
    .filter { Files.isDirectory(it) }
    .filter { child -> scriptPaths.any { it.startsWith(child) } }
    .map { it to rootPath.relativize(it).toGradleCoordinates() }
    .filterNot { (_, coordinates) -> coordinates in ignored }
    .map { (path, coordinates) ->
        ModuleNode(
            name = path.name,
            path = coordinates,
            children = path.findChildModules(ignored, scriptPaths, rootPath)
        )
    }

internal fun ModuleNode.hasChildren(): Boolean =
    children.firstOrNull() != null

internal fun ModuleNode.hasNoChildren(): Boolean =
    children.firstOrNull() == null

internal fun ModuleNode.walk(): Sequence<ModuleNode> =
    children + children.flatMap { sequenceOf(this) + it.walk() }

private fun Path.isValidScript() =
    isGroovyBuildScript() or isKotlinBuildScript()

private fun Path.isGroovyBuildScript() =
    toString().endsWith("build.gradle")

private fun Path.isKotlinBuildScript() =
    toString().endsWith("build.gradle.kts")