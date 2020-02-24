package com.pablisco.gradle.automodule

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBeEqualTo
import org.amshove.kluent.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime

class AutoModulePluginTest {

    @Test
    fun `generates modules code WITH single module`(@TempDir projectDir: File) {
        projectDir.copyProject("single_module")

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }


    @Test
    fun `generates modules code WITH multiple modules`(@TempDir projectDir: File) {
        projectDir.copyProject("multiple_modules")

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Test
    fun `generates modules code WITH nested modules`(@TempDir projectDir: File) {
        projectDir.copyProject("nested_modules")

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Test
    fun `ignores modules`(@TempDir projectDir: File) {
        projectDir.copyProject("ignore_modules")

        val result = projectDir.runGradle()

        result.output shouldNotContain "Project ':configIgnored'"
        result.output shouldNotContain "Project ':extensionIgnored'"
        result.output shouldContain "Project ':included'"
    }

    @Test
    fun `can use from groovy gradle script`(@TempDir projectDir: File) {
        projectDir.copyProject("groovy_support")

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Test
    fun `can change root module name`(@TempDir projectDir: File) {
        projectDir.copyProject("custom_root_module_name")

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    @Test
    fun `no code generation occurs with cache enabled`(@TempDir projectDir: File) {
        projectDir.copyProject("single_module")

        projectDir.runGradle()
        val initialRunLastModified = projectDir.modulesKt.lastModified
        projectDir.runGradle()
        val cachedRunLastModified = projectDir.modulesKt.lastModified

        initialRunLastModified shouldBeEqualTo cachedRunLastModified
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    @Test
    fun `code generation occurs with cache is not enabled`(@TempDir projectDir: File) {
        projectDir.copyProject("cache_disabled")

        projectDir.runGradle()
        val initialRunLastModified = projectDir.modulesKt.lastModified
        projectDir.runGradle()
        val secondRunLastModified = projectDir.modulesKt.lastModified

        initialRunLastModified shouldNotBeEqualTo secondRunLastModified
    }

}

private val File.modulesKt: Path
    get() = toPath().resolve("buildSrc/src/main/kotlin/modules.kt")

private val Path.lastModified: FileTime
    get() = Files.getLastModifiedTime(this)

private fun BuildResult.shouldBeSuccess() =
    task(":projects")!!.outcome shouldBeEqualTo SUCCESS

private fun File.copyProject(path: String) {
    resource(path).copyRecursively(target = this, overwrite = true)
    removeNoWarningExtensions()
}

private fun File.runGradle(vararg args: String = emptyArray()): BuildResult =
    GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .withArguments(listOf("projects", "--stacktrace") + args)
        .forwardOutput()
        .build()

/**
 * Kts files are compiled by the IDE, so adding the .nowarn extension to a failing file will
 * allow to ignore it by the IDE. This will restore the file to it's former extension.
 */
private fun File.removeNoWarningExtensions() {
    walkTopDown().forEach { file ->
        if (file.extension == "nowarn") {
            val newPath = file.absolutePath.replace(
                oldValue = ".nowarn",
                newValue = "",
                ignoreCase = true
            )
            file.renameTo(File(newPath))
        }
    }
}

private fun resource(path: String): File = File(resourcesFile, path)

private val resourcesFile = File(
    AutoModulePluginTest::class.java.classLoader.getResource(".")!!.path
)
