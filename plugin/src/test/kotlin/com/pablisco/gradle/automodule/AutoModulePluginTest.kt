package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.filetree.fileTree
import com.pablisco.gradle.automodule.gradle.kotlinModule
import com.pablisco.gradle.automodule.gradle.runGradle
import com.pablisco.gradle.automodule.gradle.runGradleProjects
import com.pablisco.gradle.automodule.gradle.shouldBeSuccess
import com.pablisco.gradle.automodule.utils.*
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotContain
import org.jetbrains.kotlin.konan.file.recursiveCopyTo
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime

class AutoModulePluginTest {

    @Test
    fun `generates modules code WITH simple module`() = testCase("simple_module") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `generates modules code WITH multiple modules`() = testCase("multiple_modules") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `generates modules code WITH nested modules`() = testCase("nested_modules") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `versions are loaded WITH custom versions location`() = testCase("custom_versions_location") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `versions are loaded WITH remote versions location`() = testCase("remote_versions_location") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `ignores modules`() = testCase("ignore_modules") {
        val result = workingDir.runGradleProjects()

        result.output shouldNotContain "Project ':settingsKtIgnored'"
        result.output shouldNotContain "Project ':extensionIgnored'"
        result.output shouldContain "Project ':included'"
    }

    @Test
    fun `can use from groovy gradle script`() = testCase("groovy_support") {
        val result = workingDir.runGradleProjects()

        result.shouldBeSuccess()
    }

    @Test
    fun `no code generation occurs with cache enabled`() = testCase(
        path = "simple_module",
        workingPath = "no_code_gen_with_cache"
    ) {
        val modulesKt = workingDir.modulesKt("no_code_gen_with_cache")
        workingDir.runGradleProjects()
        val initialRunLastModified = modulesKt.lastModified
        workingDir.runGradleProjects()
        val cachedRunLastModified = modulesKt.lastModified

        initialRunLastModified shouldBeEqualTo cachedRunLastModified
    }

    @Test
    fun `generates task for a given template`() = testCase("template_task") {
        workingDir.runGradle {
            withArguments("createDefaultModule", "--templateDirectory=simpleModule")
        }

        check(workingDir.resolve("simpleModule/build.gradle.kts").exists())
    }

    @Test
    fun `files are generated after manual deletion with cache enabled`() = testCase(
        path = "simple_module",
        workingPath = "files_are_generated_after_manual_delete"
    ) {
        val modulesKt = workingDir.modulesKt("files_are_generated_after_manual_delete")
        workingDir.runGradleProjects()
        modulesKt.delete()
        workingDir.runGradleProjects()

        check(modulesKt.exists())
    }

    @Test
    fun `files are generated after changing script with cache enabled`() = testCase(
        path = "simple_module",
        workingPath = "files_are_generated_after_script_changes"
    ) {
        workingDir.runGradleProjects()

        workingDir.fileTree().kotlinModule("newModule")

        val output = workingDir.runGradleProjects().output

        output shouldContain "Project ':newModule'"
    }

    @Test
    fun `classpath includes build modules`() = testCase("build_modules") {
        workingDir.resolve("gradle/buildStuff").addLocalRepository()
        val result = workingDir.runGradleProjects()

        result.shouldBeSuccess()
        result.output shouldContain "Hello Gradle!"
    }

}

class TestCase(
    path: String,
    workingPath: String,
    projectDir: Path = Paths.get(".").toAbsolutePath(),
    val testCaseDir: Path = projectDir.resolve("src/test/resources/test-cases/$path"),
    val workingDir: Path = projectDir.resolve("build/test-workspace/$workingPath")
)

private fun testCase(
    path: String,
    workingPath: String = path,
    block: TestCase.() -> Unit
) {
    TestCase(path, workingPath).apply {
        workingDir.deleteRecursively()
        workingDir.createDirectories()
        testCaseDir.recursiveCopyTo(workingDir)
        workingDir.addLocalRepository()
    }.run(block)
}

private fun Path.addLocalRepository() {
    with(resolve("settings.gradle.kts")) {
        write(
            """autoModule { pluginRepository(file("${Paths.get("../repo").toRealPath()}")) }""",
            StandardOpenOption.APPEND
        )
    }
}

private fun Path.modulesKt(projectName: String): Path =
    resolve(".gradle/automodule/${projectName}-modules/src/main/kotlin/modules.kt")

private val Path.lastModified: FileTime
    get() = Files.getLastModifiedTime(this)
