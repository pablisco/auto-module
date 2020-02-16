package com.pablisco.gradle.automodule

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
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

        result.output.shouldContainProjects(":singleModule")

        projectDir.modulesKt shouldBeEqualTo resourceText("expected/single_module/modules.kt")
    }

    @Test
    fun `generates modules code WITH multiple modules`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject(
            "multiple_modules",
            ":moduleThree:tasks"
        )

        result.output.shouldContainProjects(":moduleOne", ":moduleTwo", ":moduleThree")

        result.task(":moduleThree:tasks")?.outcome shouldBeEqualTo SUCCESS
    }

    @Test
    fun `generates modules code WITH nested modules`(@TempDir projectDir: File) {
        val result = projectDir.givenAProject("nested_modules")

        result.output.shouldContainProjects(":moduleOne", ":parent:moduleTwo")

        projectDir.modulesKt shouldBeEqualTo resourceText("expected/nested_modules/modules.kt")
    }

}

private fun String.shouldContainProjects(vararg modules: String) {
    modules.forEach {
        this shouldContain "Project '$it'"
    }
}

private fun File.givenAProject(path: String, vararg extraParams: String): BuildResult {
    resource(path).copyRecursively(target = this)

    return GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .withArguments(*(arrayOf("projects", "--stacktrace") + extraParams))
        .build()
        .apply { println(output) }
}

private val File.modulesKt: String
    get() = File(this, "buildSrc/src/main/kotlin/modules.kt").readText()

private fun resource(path: String): File = File(resourcesFile, path)
private fun resourceText(path: String): String = File(resourcesFile, path).readText()

private val resourcesFile = File(
    AutoModulePluginTest::class.java.classLoader.getResource(".")!!.path
)
