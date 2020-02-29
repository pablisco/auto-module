package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.checkPropertyIsNotPresentIn
import com.pablisco.gradle.automodule.utils.createFile
import com.pablisco.gradle.automodule.utils.maybeReadText
import com.pablisco.gradle.automodule.utils.md5
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.initialization.Settings

class AutoModulePlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        check(target.rootDir.resolve("buildSrc/build.gradle.kts").exists()) {
            "Missing buildSrc/build.gradle.kts, this is required by autoModule"
        }
        val autoModuleScope = AutoModuleScope(target)
        // need to evaluate the settings so it applies user defined configuration
        target.gradle.settingsEvaluated {
            with(autoModuleScope) {
                notifyIgnoredModules()
                checkRootNameIsAvailable()
                includeModulesToSettings()
                generateModuleGraph()
                addGroovySupport()
            }
        }

        target.gradle.rootProject { rootProject ->
            autoModuleScope.autoModule.templates.forEach { rootProject.createTask(it) }
        }
    }
}

private fun Project.createTask(template: AutoModuleTemplate): CreateModuleTask =
    tasks.create(
        "create${template.name.capitalize()}Module",
        CreateModuleTask::class.java,
        template
    )

private fun AutoModuleScope.checkRootNameIsAvailable() {
    checkPropertyIsNotPresentIn<DependencyHandler>(autoModule.entryPointName) {
        "Can't use \"${autoModule.entryPointName}\" as \"rootModuleName\" since " +
                "DependencyHandler already has that property"
    }
}

private fun AutoModuleScope.notifyIgnoredModules() {
    if (autoModule.ignored.isNotEmpty()) {
        log("Ignoring modules: ${autoModule.ignored}")
    }
}

private fun AutoModuleScope.addGroovySupport() {
    settings.gradle.beforeProject { project ->
        project.extensions.add(
            autoModule.entryPointName,
            GroovyRootModule(project.dependencies)
        )
    }
}

private fun AutoModuleScope.generateModuleGraph() {
    if (autoModule.cacheEnabled and isCached() and isCodeUpToDate()) {
        log("Module Graph is UP-TO-DATE")
    } else {
        rootModule.writeTo(
            directory = codeOutputDirectory,
            fileName = autoModule.modulesFileName,
            rootModuleName = autoModule.entryPointName
        )
        saveChecksums()
        log("Module Graph saved to $codeOutputDirectory/${autoModule.modulesFileName}.kt")
    }
}

private fun AutoModuleScope.isCached(): Boolean =
    "$scriptsHash" == directoriesHashFile.maybeReadText()

private fun AutoModuleScope.isCodeUpToDate(): Boolean =
    generatedCodeMd5 == generatedMd5File.maybeReadText()

private val AutoModuleScope.generatedCodeMd5: String
    get() = codeOutputDirectory.resolve(autoModule.modulesFileName + ".kt").md5()

private fun AutoModuleScope.saveChecksums() {
    directoriesHashFile.createFile(content = "$scriptsHash")
    generatedMd5File.createFile(content = generatedCodeMd5)
}

private fun AutoModuleScope.includeModulesToSettings() {
    rootModule.walk().mapNotNull { it.path }.forEach { path ->
        log("include($path)")
        settings.include(path)
    }
}

/**
 * Default implementation of [ModuleNode.hashCode] doesn't take into account sequences.
 */
private fun ModuleNode.hashcode(): Int =
    name.hashCode() + path.hashCode() +
            children.map { it.hashcode() }.fold(0) { acc, it -> acc + it }