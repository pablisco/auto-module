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
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime

class AutoModulePluginTest {

    @Test
    fun `generates modules code WITH single module`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript
            "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.3.61" }
                dependencies {
                    implementation(local.singleModule)
                }
            """.trimIndent()
            buildSrcModule()
            "singleModule" {
                "build.gradle.kts"()
            }
        }

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Test
    fun `generates modules code WITH multiple modules`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript
            "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.3.61" }
                dependencies {
                    implementation(local.moduleOne)
                    implementation(local.moduleTwo)
                }
            """.trimIndent()
            buildSrcModule()
            "moduleOne" {
                "build.gradle.kts"()
            }
            "moduleTwo" {
                "build.gradle.kts"()
            }
        }

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Test
    fun `generates modules code WITH nested modules`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript
            "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.3.61" }
                dependencies {
                    implementation(local.moduleOne)  
                    implementation(local.parent.moduleTwo)
                    implementation(local.parent.moduleTwo.nested)
                }
            """.trimIndent()
            buildSrcModule()
            "moduleOne" {
                "build.gradle.kts"()
            }
            "parent" {
                "moduleTwo" {
                    "nested" {
                        "build.gradle.kts"()
                    }
                    "build.gradle.kts"()
                }
            }
        }

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Test
    fun `ignores modules`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript + """
                autoModule { 
                    ignore(":configIgnored") 
                }
            """.trimIndent()
            "build.gradle.kts"()
            buildSrcModule()
            "configIgnored" {
                "build.gradle.kts"()
            }
            "extensionIgnored" {
                "build.gradle.kts.ignored"()
            }
            "included" {
                "build.gradle.kts"()
            }
        }

        val result = projectDir.runGradle()

        result.output shouldNotContain "Project ':configIgnored'"
        result.output shouldNotContain "Project ':extensionIgnored'"
        result.output shouldContain "Project ':included'"
    }

    @Test
    fun `can use from groovy gradle script`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript + """
                autoModule { 
                    ignore(":configIgnored")
                }
            """.trimIndent()
            "build.gradle.kts"()
            buildSrcModule()
            "groovyModule" {
                "build.gradle" += """
                    apply plugin: 'java'
                    dependencies { implementation(local.kotlinModule) }
                """.trimIndent()
            }
            "kotlinModule" {
                "build.gradle.kts"()
            }
        }

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Test
    fun `can change root module name`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript + """
                autoModule {
                    entryPointName = "banana"
                }
            """.trimIndent()
            "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.3.61" }
                dependencies {
                    implementation(banana.singleModule)
                }
            """.trimIndent()
            buildSrcModule()
            "singleModule" {
                "build.gradle.kts"()
            }
        }

        val result = projectDir.runGradle()

        result.shouldBeSuccess()
    }

    @Test
    fun `no code generation occurs with cache enabled`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript
            "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.3.61" }
                dependencies {
                    implementation(local.singleModule)
                }
            """.trimIndent()
            buildSrcModule()
            "singleModule" {
                "build.gradle.kts"()
            }
        }

        projectDir.runGradle()
        val initialRunLastModified = projectDir.modulesKt.lastModified
        projectDir.runGradle()
        val cachedRunLastModified = projectDir.modulesKt.lastModified

        initialRunLastModified shouldBeEqualTo cachedRunLastModified
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    @Test
    fun `code generation occurs with cache is not enabled`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript + """
                autoModule {
                    cacheEnabled = false
                }
            """.trimIndent()
            "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.3.61" }
                dependencies {
                    implementation(local.singleModule)
                }
            """.trimIndent()
            buildSrcModule()
            "singleModule" {
                "build.gradle.kts"()
            }
        }

        projectDir.runGradle()
        val initialRunLastModified = projectDir.modulesKt.lastModified
        projectDir.runGradle()
        val secondRunLastModified = projectDir.modulesKt.lastModified

        initialRunLastModified shouldNotBeEqualTo secondRunLastModified
    }

}

private fun FileTreeScope.buildSrcModule() {
    "buildSrc" {
        "build.gradle.kts" += """
            repositories { jcenter() }
            plugins { `kotlin-dsl` }
        """.trimIndent()
    }
}

private val Path.modulesKt: Path
    get() = resolve("buildSrc/src/main/kotlin/modules.kt")

private val Path.lastModified: FileTime
    get() = Files.getLastModifiedTime(this)

private fun BuildResult.shouldBeSuccess() =
    task(":projects")!!.outcome shouldBeEqualTo SUCCESS

private fun Path.runGradle(vararg args: String = emptyArray()): BuildResult =
    GradleRunner.create()
        .withProjectDir(this.toFile())
        .withPluginClasspath()
        .withArguments(listOf("projects", "--stacktrace") + args)
        .forwardOutput()
        .build()

val defaultSettingsGradleScript = """
    plugins {
        id("com.pablisco.gradle.automodule")
    }
""".trimIndent()
