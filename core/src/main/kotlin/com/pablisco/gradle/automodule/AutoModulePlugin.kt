package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.checkPropertyIsNotPresentIn
import com.pablisco.gradle.automodule.utils.createFile
import org.gradle.api.Plugin
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class AutoModulePlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val autoModuleScope = AutoModuleScope(target)
        // need to evaluate the settings so it applies user defined configuration
        target.gradle.settingsEvaluated {
            with(autoModuleScope) {
                notifyIgnoredModules()
                checkRootNameIsAvailable()
                if (rootModule.hasNoChildren()) {
                    log("No modules found in $rootDir")
                } else {
                    includeModulesToSettings()
                    generateModuleGraph()
                    addGroovySupport()
                }
            }
        }
    }

}

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
    gradle.beforeProject { project ->
        project.extensions.add(
            autoModule.entryPointName,
            GroovyRootModule(project.dependencies)
        )
    }
}

private fun AutoModuleScope.generateModuleGraph() {
    if (autoModule.cacheEnabled and isCached()) {
        log("Module Graph is UP-TO-DATE")
    } else {
        rootModule.writeTo(
            directory = codeOutputDirectory,
            fileName = autoModule.modulesFileName,
            rootModuleName = autoModule.entryPointName
        )
        saveCachedChecksum()
        log("Module Graph saved to $codeOutputDirectory/${autoModule.modulesFileName}.kt")
    }
}

private fun AutoModuleScope.isCached(): Boolean =
    cacheChecksum.let { "${rootModule.hashcode()}" == it }

private fun AutoModuleScope.saveCachedChecksum() {
    checkSumFile.createFile(content = "${rootModule.hashcode()}")
}

private fun AutoModuleScope.includeModulesToSettings() {
    rootModule.walk().mapNotNull { it.path }.forEach { path ->
        log("include($path)")
        include(path)
    }
}

private fun AutoModuleScope.log(message: String) {
    logger.log(autoModule.logLevel, "[Auto-Module] $message")
}

private val logger: Logger by lazy { Logging.getLogger(AutoModulePlugin::class.java) }

/**
 * Default implementation of [ModuleNode.hashCode] doesn't take into account sequences.
 */
private fun ModuleNode.hashcode(): Int =
    name.hashCode() + path.hashCode() +
            children.map { it.hashcode() }.fold(0) { acc, it -> acc + it }