package com.pablisco.gradle.automodule.gradle

import com.pablisco.gradle.automodule.filetree.FileTreeScope

internal fun FileTreeScope.kotlinModule(name: String, vararg dependencies: String) {
    name {
        "build.gradle.kts" += """
plugins { kotlin("jvm") }

${writeDependencies(dependencies)}
"""
    }
}

private fun writeDependencies(paths: Array<out String>): String = when {
    paths.isEmpty() -> ""
    else -> """
        dependencies {
            ${paths.joinToString("\n") { "implementation(project(automodule.$it))" }}
        }
    """.trimIndent()
}

internal const val defaultSettingsGradleScript = """
plugins {
    id("com.pablisco.gradle.automodule")
}
"""