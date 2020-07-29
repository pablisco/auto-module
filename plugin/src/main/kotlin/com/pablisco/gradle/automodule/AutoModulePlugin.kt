package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.filetree.fileTree
import com.pablisco.gradle.automodule.utils.createFile
import com.pablisco.gradle.automodule.utils.log
import com.pablisco.gradle.automodule.utils.maybeReadText
import com.pablisco.gradle.automodule.utils.md5
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.*

class AutoModulePlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val settingsScope = SettingsScope(target)
        // need to evaluate the settings so it applies user defined configuration
        target.gradle.settingsEvaluated {
            settingsScope.apply()
        }
    }

}

private fun SettingsScope.apply() {
    notifyIgnoredModules()
    includeModulesToSettings()
    generateModuleGraph()
    includeGeneratedGraphModule()

    gradle.rootProject {
        autoModule.templates.forEach { template -> createTask(template) }
        buildscript {
            repositories {
                autoModule.pluginRepositoryPath?.let { maven(url = it) }
                gradlePluginPortal()
            }
            dependencies {
                classpath("automodule:graph")
            }
        }
        extensions.create<GroovyAutoModules>("autoModules")
        tasks.create<Delete>("cleanAutoModule") {
            delete(directoriesHashFile, generatedMd5File)
        }
    }
}

private fun SettingsScope.includeGeneratedGraphModule() {
    includeBuild(generatedGraphModule) {
        dependencySubstitution {
            substitute(module("automodule:graph")).with(project(":"))
        }
    }
}

private fun Project.createTask(template: AutoModuleTemplate): CreateModuleTask =
    tasks.create(
        "create${template.name.capitalize()}Module",
        CreateModuleTask::class.java,
        template
    )

private fun SettingsScope.notifyIgnoredModules() {
    if (autoModule.ignored.isNotEmpty()) {
        log("Ignoring modules: ${autoModule.ignored}")
    }
}

private fun SettingsScope.generateModuleGraph() {
    if (isCached() and isCodeUpToDate()) {
        log("Module Graph is UP-TO-DATE")
    } else {
        val extraRepository = autoModule.pluginRepositoryPath?.let { "maven(url = \"${it}\")" } ?: ""
        generatedGraphModule.fileTree {
            "settings.gradle.kts" += "rootProject.name = \"module-graph\""
            "build.gradle.kts" += """
                buildscript{
                    repositories {
                        $extraRepository
                        gradlePluginPortal()
                    }
                }
                plugins {
                    kotlin("jvm") version "1.3.72"
                }

                repositories {
                    jcenter()
                    $extraRepository
                    gradlePluginPortal()
                }

                dependencies {
                    implementation(kotlin("stdlib"))
                    api("com.pablisco.gradle.automodule:plugin:0.13")
                }
            """.trimIndent()
        }
        rootModule.writeTo(
            directory = generatedGraphSource,
            fileName = modulesFileName
        )
        saveChecksums()
        log("Module Graph saved to $generatedGraphSource/$modulesFileName.kt")
    }
}

private fun SettingsScope.isCached(): Boolean =
    "$scriptsHash" == directoriesHashFile.maybeReadText()

private fun SettingsScope.isCodeUpToDate(): Boolean =
    generatedCodeMd5 == generatedMd5File.maybeReadText()

private val SettingsScope.generatedCodeMd5: String
    get() = generatedGraphSource.resolve("$modulesFileName.kt").md5()

private fun SettingsScope.saveChecksums() {
    directoriesHashFile.createFile(content = "$scriptsHash")
    generatedMd5File.createFile(content = generatedCodeMd5)
}

private fun SettingsScope.includeModulesToSettings() {
    rootModule.walk().mapNotNull { it.path }
        .filterNot { it in autoModule.ignored }
        .forEach { path ->
        log("include($path)")
        include(path)
    }
}
