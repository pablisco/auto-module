package com.pablisco.gradle.automodule

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AutoModulePluginTest {

    @Test
    fun `notifies plugin lifecycle WHEN no modules available`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject("no_modules")

        with(result) {
            output shouldContain "[Auto-Module] Starting"
            output shouldContain "[Auto-Module] No modules found in "
        }
    }

    @Test
    fun `notifies plugin lifecycle WHEN modules are available`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject("single_module")

        with(result) {
            output shouldContain "[Auto-Module] Starting"
            output shouldContain "[Auto-Module] Generated modules graph in "
        }
    }

    @Test
    fun `generates modules code WITH single module`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject("single_module")

        result.shouldBeSuccess()
    }


    @Test
    fun `generates modules code WITH multiple modules`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject("multiple_modules")

        result.shouldBeSuccess()
    }

    @Test
    fun `generates modules code WITH nested modules`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject("nested_modules")

        result.shouldBeSuccess()
    }

    @Test
    fun `ignores modules`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject("ignore_modules")

        result.output shouldNotContain "Project ':configIgnored'"
        result.output shouldNotContain "Project ':extensionIgnored'"
        result.output shouldContain "Project ':included'"
    }

    @Test
    fun `can use from groovy gradle script`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject("groovy_support")

        result.shouldBeSuccess()
    }

}

private fun BuildResult.shouldBeSuccess() =
    task(":projects")!!.outcome shouldBeEqualTo SUCCESS

private fun File.givenAProject(path: String, vararg extraParams: String): BuildResult {
    resource(path).copyRecursively(target = this)

    removeNoWarningExtensions()

    return GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .withArguments(*(arrayOf("projects", "--stacktrace") + extraParams))
        .forwardOutput()
        .build()
}

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
