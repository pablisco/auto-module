package com.pablisco.gradle.automodule.gradle

import com.pablisco.gradle.automodule.filetree.FileTreeScope

internal fun FileTreeScope.buildSrcModule() {
    file(
        path = "buildSrc/build.gradle.kts",
        content = """
            repositories { jcenter() }
            plugins { `kotlin-dsl` }
        """.trimIndent()
    )
}

internal fun FileTreeScope.emptyKotlinModule(name: String = "simpleModule") {
    name { emptyBuildGradleKts() }
}

internal fun FileTreeScope.emptyBuildGradleKts() {
    "build.gradle.kts"()
}

internal fun FileTreeScope.kotlinGradleScript(
    vararg dependencies: String = arrayOf("simpleModule")
) {
    "build.gradle.kts" += """
        plugins { kotlin("jvm") version "1.3.61" }
        ${writeDependencies(dependencies)}
    """.trimIndent()
}

private fun writeDependencies(paths: Array<out String>): String = when {
    paths.isEmpty() -> ""
    else -> """
        dependencies {
            ${paths.joinToString("\n") { "implementation(local.$it)" }}
        }
    """.trimIndent()
}

internal val defaultSettingsGradleScript = """
    plugins {
        id("com.pablisco.gradle.automodule")
    }
""".trimIndent()