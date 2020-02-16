package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.createFile
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AutoModulePluginTest {

    @Test
    fun `notifies plugin lifecycle WHEN no modules available`(
        @TempDir projectDir: File
    ) {
        val result = projectDir.givenAProjectWith(
            "settings.gradle.kts" to minimumSettingGradleKts
        )

        val output = result.output

        output shouldContain "[Auto-Module] Starting"
        output shouldContain "[Auto-Module] No modules found in "
    }

    @Test
    fun `notifies plugin lifecycle WHEN modules are available`(
        @TempDir projectDir: File
    ) {
        val result = projectDir.givenAProjectWith(
            "settings.gradle.kts" to minimumSettingGradleKts,
            "singleModule/build.gradle.kts" to emptyBuildGradleKts
        )

        val output = result.output

        output shouldContain "[Auto-Module] Starting"
        output shouldContain "[Auto-Module] Generated modules graph in "
    }

    @Test
    fun `generates modules code WITH single module`(
        @TempDir projectDir: File
    ) {
        val result = projectDir.givenAProjectWith(
            "settings.gradle.kts" to minimumSettingGradleKts,
            "singleModule/build.gradle.kts" to emptyBuildGradleKts
        )

        val output = result.output

        output.shouldContainProjects(":singleModule")

        val modulesKt = File(projectDir, "buildSrc/src/main/kotlin/modules.kt").readText()

        modulesKt shouldBeEqualTo """
            import org.gradle.api.artifacts.dsl.DependencyHandler
            import org.gradle.kotlin.dsl.project

            val DependencyHandler.local: Local
                get() = Local(this)

            class Local(dh: DependencyHandler) {
                val singleModule = dh.project(":singleModule")
            }
        """.trimIndent()
    }

    @Test
    fun `generates modules code WITH multiple modules`(
        @TempDir projectDir: File
    ) {
        val result = projectDir.givenAProjectWith(
            "settings.gradle.kts" to minimumSettingGradleKts,
            "moduleOne/build.gradle.kts" to emptyBuildGradleKts,
            "moduleTwo/build.gradle.kts" to emptyBuildGradleKts
        )

        val output = result.output

        output.shouldContainProjects(":moduleOne", ":moduleTwo")

        val modulesKt = File(projectDir, "buildSrc/src/main/kotlin/modules.kt").readText()

        modulesKt shouldBeEqualTo """
            import org.gradle.api.artifacts.dsl.DependencyHandler
            import org.gradle.kotlin.dsl.project

            val DependencyHandler.local: Local
                get() = Local(this)

            class Local(dh: DependencyHandler) {
                val moduleTwo = dh.project(":moduleTwo")
                val moduleOne = dh.project(":moduleOne")
            }
        """.trimIndent()
    }

    @Test
    fun `generates modules code WITH nested modules`(
        @TempDir projectDir: File
    ) {
        val result = projectDir.givenAProjectWith(
            "settings.gradle.kts" to minimumSettingGradleKts,
            "moduleOne/build.gradle.kts" to emptyBuildGradleKts,
            "parent/moduleTwo/build.gradle.kts" to emptyBuildGradleKts
        )

        val output = result.output

        output.shouldContainProjects(":moduleOne", ":parent:moduleTwo")

        val modulesKt = File(projectDir, "buildSrc/src/main/kotlin/modules.kt").readText()

        modulesKt shouldBeEqualTo """
            import org.gradle.api.artifacts.dsl.DependencyHandler
            import org.gradle.kotlin.dsl.project

            val DependencyHandler.local: Local
                get() = Local(this)

            class Local(dh: DependencyHandler) {
                val moduleOne = dh.project(":moduleOne")
                val parent = Parent(dh)
            }
            
            class Parent(dh: DependencyHandler) {
                val moduleTwo = dh.project(":parent:moduleTwo")
            }
        """.trimIndent()
    }

}

private fun String.shouldContainProjects(vararg modules: String) {
    modules.forEach {
        this shouldContain "Project '$it'"
    }
}

private val minimumSettingGradleKts =
    """
      plugins {
          id("auto-module")
      }
    """.trimIndent()

private const val emptyBuildGradleKts = ""

private fun File.givenAProjectWith(
    vararg files: Pair<String, String>
): BuildResult {
    files.forEach { (path, content) ->
        createFile(path, content)
    }
    return GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .withArguments("projects", "--stacktrace")
        .build()
        .apply { println(output) }
}
