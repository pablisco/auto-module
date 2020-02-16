package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.camelCase
import com.pablisco.gradle.automodule.utils.createFile
import com.pablisco.gradle.automodule.utils.snakeCase
import java.io.File

internal fun Collection<ModuleNode>.writeTo(file: File) {
    file.delete()
    file.createFile(content = cleanup().toCode())
}

private fun Collection<ModuleNode>.toCode() = """
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

val DependencyHandler.modules: Modules
    get() = Modules(this)

class Modules(dh: DependencyHandler) {
    ${joinToString("\n    ") { it.toProperty() }}
}

${flatten().filterNot { it.children.isEmpty() }.joinToString("\n") {
    it.children.toModuleType(it.name)
}}

""".trim()

private fun Collection<ModuleNode>.flatten(): Sequence<ModuleNode> =
    asSequence().fold(emptySequence()) { acc, it ->
        acc + it + it.children.asSequence()
    }

private fun ModuleNode.toProperty() = when {
    children.isEmpty() -> """val ${name.snakeCase()} = dh.project("$path")"""
    else -> """val ${name.snakeCase()} = ${name.camelCase()}(dh)"""
}

private fun List<ModuleNode>.toModuleType(name: String) = """
class ${name.camelCase()}(dh: DependencyHandler) {
    ${joinToString("\n    ") { it.toProperty() }}
}
""".trimStart()