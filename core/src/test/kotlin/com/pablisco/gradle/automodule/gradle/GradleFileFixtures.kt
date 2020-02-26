package com.pablisco.gradle.automodule.gradle

import com.pablisco.gradle.automodule.filetree.FileTreeScope

internal fun FileTreeScope.buildSrcModule() {
    "buildSrc" {
        "build.gradle.kts" += """
            repositories { jcenter() }
            plugins { `kotlin-dsl` }
        """.trimIndent()
    }
}

internal val defaultSettingsGradleScript = """
    plugins {
        id("com.pablisco.gradle.automodule")
    }
""".trimIndent()