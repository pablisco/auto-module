package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.filetree.FileTreeScope
import com.pablisco.gradle.automodule.filetree.fileTree
import com.pablisco.gradle.automodule.gradle.buildSrcModule
import com.pablisco.gradle.automodule.gradle.defaultSettingsGradleScript
import com.pablisco.gradle.automodule.gradle.runGradle
import com.pablisco.gradle.automodule.utils.createDirectories
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.system.measureTimeMillis

private typealias Modules = Sequence<String>

class AutoModulePluginBenchmarks {

    @Test
    fun `measure manual vs AutoModule`(
        @TempDir tmp: Path
    ) = measure {
        val manualProjectDir = tmp.resolve("manual").createDirectories()
        val autoModuleProjectDir = tmp.resolve("autoModule").createDirectories()

        manualProjectDir.fileTree {
            val modules = generateModules()
            manualSettings(modules)
            "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.3.61" }
                dependencies {
                    ${dependencies(modules.asManualNotation())}
                }
            """.trimIndent()
            buildSrcModule()
            createModules(modules)
        }

        autoModuleProjectDir.fileTree {
            val modules = generateModules()
            "settings.gradle.kts" += defaultSettingsGradleScript
            "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.3.61" }
                dependencies {
                    ${dependencies(modules.asAutoModuleNotation())}
                }
            """.trimIndent()
            buildSrcModule()
            createModules(modules)
        }

        // warm up
        autoModuleProjectDir.runGradle()
        manualProjectDir.runGradle()

        "AutoModule build"(runCount = 10) { autoModuleProjectDir.runGradle() }
        "Manual build"(runCount = 10) { manualProjectDir.runGradle() }
    }

}

private fun generateModules(): Modules =
    (0..100).asSequence().map { "module$it" }

private fun FileTreeScope.manualSettings(modules: Modules) {
    "settings.gradle.kts" += modules
        .asManualNotation()
        .joinToString("\n") { """include($it)""" }
}

private fun FileTreeScope.createModules(modules: Modules) {
    modules.forEach { moduleName ->
        moduleName {
            "build.gradle.kts"()
        }
    }
}

private fun dependencies(modules: Sequence<String>): String =
    modules.joinToString("\n") { """implementation($it)""" }

private fun Modules.asManualNotation(): Sequence<String> =
    map { "\":$it\"" }

private fun Modules.asAutoModuleNotation(): Sequence<String> =
    map { "local.$it" }

private fun measure(block: MeasureScope.() -> Unit) {
    println(MeasureScope().apply(block).log)
}

private class MeasureScope {

    var log = "[Measurements]\n"

    operator fun String.invoke(block: () -> Unit) {
        val timeTaken = measureTimeMillis(block)
        log += "\t$this: ${timeTaken}ms\n"
    }

    operator fun String.invoke(runCount: Int, block: () -> Unit) {
        val times = (0 until runCount).map { measureTimeMillis(block) }
        val max = times.max() ?: -1
        val min = times.min() ?: -1
        val average = times.average()
        val error = max - min
        log += "\t$this: avg ${average}ms, max ${max}ms, min ${min}ms, error ${error}ms\n"
    }

}
