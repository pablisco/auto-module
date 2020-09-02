package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.filetree.fileTree
import com.pablisco.gradle.automodule.utils.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionSelector
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import java.io.File

private const val version = "0.15"

class AutoModulePlugin : Plugin<Settings> {

    private val autoModule = AutoModule()

    override fun apply(target: Settings) {
        target.extensions.add("autoModule", autoModule)
        SettingsScope(autoModule, target).whenEvaluated {
            val versions = Versions(rootDir, autoModule.versions)
            addGlobalExtension("versions", extension = versions)
            notifyIgnoredModules()
            includeModulesToSettings()
            generateModuleGraph()
            injectVersionResolution(versions)
            includeGeneratedGraphModule()
            includeBuildModules()
            createTemplateTasks()
            addDebugArtifactRepository()
            addGroovySupport()
            createCleanTask()
        }
    }

}

private fun SettingsScope.createCleanTask() = gradle.rootProject {
    tasks.create<Delete>("cleanAutoModule") {
        delete(directoriesHashFile, generatedMd5File)
    }
}

private fun SettingsScope.addGroovySupport() = gradle.rootProject {
    extensions.create<GroovyAutoModules>("autoModules")
}

private fun SettingsScope.addDebugArtifactRepository() = gradle.rootProject {
    buildscript {
        repositories {
            autoModule.pluginRepositoryPath?.let { maven(url = it) }
            gradlePluginPortal()
        }
    }
}

private fun SettingsScope.includeBuildModules() {
    val buildModulesRoot = rootDir.resolve(autoModule.buildModulesRoot)

    val buildModules = buildModulesRoot.children()
        .filter { it.isDirectory }
        .filter { dir -> dir.children().any { it.name == "build.gradle.kts" } }
    buildModules.forEach { dir ->
        includeBuild(dir) {
            dependencySubstitution {
                substitute(module("gradle:${dir.name}:local")).with(project(":"))
            }
        }
        gradle.rootProject {
            buildscript {
                dependencies {
                    classpath("gradle:${dir.name}:local")
                }
            }
        }
    }
}

private fun File.children(): List<File> = listFiles()?.toList() ?: emptyList()

private fun SettingsScope.whenEvaluated(f: SettingsScope.() -> Unit) {
    gradle.settingsEvaluated { f() }
}

private fun SettingsScope.includeGeneratedGraphModule() {
    includeBuild(generatedGraphModule) {
        dependencySubstitution {
            substitute(module("automodule:graph:local")).with(project(":"))
        }
    }
    gradle.rootProject {
        buildscript {
            dependencies { classpath("automodule:graph:local") }
        }
    }
}

private fun SettingsScope.createTemplateTasks() = gradle.rootProject {
    afterEvaluate {
        autoModule.templates.forEach { template -> createTemplateTask(template) }
    }
}

private fun Project.createTemplateTask(template: AutoModuleTemplate): CreateModuleTask =
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

private fun ModuleVersionSelector.hasVersion(): Boolean =
    version?.takeIf { it.isNotEmpty() } != null

private fun SettingsScope.injectVersionResolution(versions: Versions) {
    val injectDependencies: Configuration.() -> Unit = {
        resolutionStrategy.eachDependency {
            requested.takeUnless { it.hasVersion() }?.also { versionSelector ->
                useVersion(versions.getDependencyVersion(versionSelector))
            }
        }
    }

    gradle.allprojects {
        buildscript { configurations.all(injectDependencies) }
        configurations.all(injectDependencies)
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
        val extraRepository = autoModule.pluginRepositoryPath
            ?.let { "maven(url = \"${it}\")" } ?: ""
        generatedGraphModule.fileTree {
            "settings.gradle.kts" += "rootProject.name = \"$graphModuleName\""
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
