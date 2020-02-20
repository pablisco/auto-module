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
    children = findChildModules(ignored)
)

internal fun Path.findChildModules(
    ignored: List<String>,
    rootPath: Path = this,
    isValidScript: Path.() -> Boolean = Path::isValidScript
): Sequence<ModuleNode> = list()
    .filterNot { it.name == "buildSrc" }
    .filterNot { it.name.startsWith(".") }
    .filter { Files.isDirectory(it) }
    .filter { child -> child.walk().any { it.isValidScript() } }
    .map {
        ModuleNode(
            name = it.name,
            path = rootPath.relativize(it).toGradleCoordinates(),
            children = it.findChildModules(ignored, rootPath)
        )
    }
    .filterNot { it.path in ignored }

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