package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.filetree.fileTree
import com.pablisco.gradle.automodule.utils.createFile
import com.pablisco.gradle.automodule.utils.log
import com.pablisco.gradle.automodule.utils.maybeReadText
import com.pablisco.gradle.automodule.utils.md5
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleVersionSelector
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import java.io.File
import java.util.*

private const val version = "0.14"

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
    injectVersionResolution()
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

private class Versions(private val propertiesFile: File) {

    private val properties = Properties().apply {
        propertiesFile.takeIf { it.exists() }?.also { load(it.reader()) }
    }

    private val keyValues = properties.mapNotNull { (k, v) ->
        if (k is String && v is String) k to v else null
    }

    fun findDependency(group: String, name: String): String =
        sequenceOf("${group}_${name}", name, group)
            .mapNotNull { properties[it] as? String }.firstOrNull()
            ?: error("no version present on $propertiesFile for plugin: ${group}:${name}")

    fun findPluginVersion(id: String): String? =
        keyValues.firstOrNull { (k, _) -> id.startsWith(k) }?.second

}

private fun ModuleVersionSelector.hasVersion(): Boolean =
    version?.takeIf { it.isNotEmpty() } != null

private fun SettingsScope.injectVersionResolution() {
    val versions = Versions(settingsDir.resolve(autoModule.versionsPropertiesFile))

    gradle.allprojects {
        configurations.all {
            resolutionStrategy.eachDependency {
                requested.takeUnless { it.hasVersion() }?.apply {
                    useVersion(versions.findDependency(group, name))
                }
            }
        }
    }

    pluginManagement {
        resolutionStrategy {
            eachPlugin {
                versions.findPluginVersion(requested.id.id)?.also { useVersion(it) }
            }
        }
    }
}

private fun SettingsScope.generateModuleGraph() {
    if (isCached() and isCodeUpToDate() and isSameVersion()) {
        log("Module Graph is UP-TO-DATE")
    } else {
        val extraRepository =
            autoModule.pluginRepositoryPath?.let { "maven(url = \"${it}\")" } ?: ""
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
                    kotlin("jvm") version "1.4.0"
                }

                repositories {
                    jcenter()
                    $extraRepository
                    gradlePluginPortal()
                }

                dependencies {
                    implementation(kotlin("stdlib"))
                    api("com.pablisco.gradle.automodule:plugin:$version")
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

private fun SettingsScope.isSameVersion(): Boolean =
    version == versionFile.maybeReadText()

private val SettingsScope.generatedCodeMd5: String
    get() = generatedGraphSource.resolve("$modulesFileName.kt").md5()

private fun SettingsScope.saveChecksums() {
    directoriesHashFile.createFile(content = "$scriptsHash")
    generatedMd5File.createFile(content = generatedCodeMd5)
    versionFile.createFile(content = version)
}

private fun SettingsScope.includeModulesToSettings() {
    rootModule.walk().mapNotNull { it.path }
        .filterNot { it in autoModule.ignored }
        .forEach { path ->
            log("include($path)")
            include(path)
        }
}
