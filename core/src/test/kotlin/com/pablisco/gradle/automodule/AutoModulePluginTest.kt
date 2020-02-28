package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.filetree.fileTree
import com.pablisco.gradle.automodule.gradle.*
import com.pablisco.gradle.automodule.utils.exists
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBeEqualTo
import org.amshove.kluent.shouldNotContain
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

        val result = projectDir.runGradleProjects()

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

        val result = projectDir.runGradleProjects()

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

        val result = projectDir.runGradleProjects()

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

        val result = projectDir.runGradleProjects()

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

        val result = projectDir.runGradleProjects()

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

        val result = projectDir.runGradleProjects()

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

        projectDir.runGradleProjects()
        val initialRunLastModified = projectDir.modulesKt.lastModified
        projectDir.runGradleProjects()
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

        projectDir.runGradleProjects()
        val initialRunLastModified = projectDir.modulesKt.lastModified
        projectDir.runGradleProjects()
        val secondRunLastModified = projectDir.modulesKt.lastModified

        initialRunLastModified shouldNotBeEqualTo secondRunLastModified
    }

    @Test
    fun `generates task for a given template`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript + """
                autoModule {
                    template("default") {
                        emptyFile( "build.gradle.kts")
                    }
                }
            """.trimIndent()
            "build.gradle.kts"()
            buildSrcModule()
        }

        projectDir.runGradle {
            withArguments("createDefaultModule", "--templateDirectory=simpleModule")
        }

        check(projectDir.resolve("simpleModule/build.gradle.kts").exists())
    }

    @Test
    fun `generates task for a given template with custom path`(@TempDir projectDir: Path) {
        projectDir.fileTree {
            "settings.gradle.kts" += defaultSettingsGradleScript + """
                autoModule {
                    template(type = "feature", path = "features") {
                        emptyFile("build.gradle.kts")
                    }
                }
            """.trimIndent()
            "build.gradle.kts"()
            buildSrcModule()
        }

        projectDir.runGradle {
            withArguments(
                "createFeatureModule",
                "--templateDirectory=settings"
            )
        }

        check(projectDir.resolve("features/settings/build.gradle.kts").exists())
    }

}

private val Path.modulesKt: Path
    get() = resolve("buildSrc/src/main/kotlin/modules.kt")

private val Path.lastModified: FileTime
    get() = Files.getLastModifiedTime(this)
