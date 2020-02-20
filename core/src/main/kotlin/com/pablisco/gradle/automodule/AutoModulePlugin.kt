package com.pablisco.gradle.automodule

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import kotlin.system.measureTimeMillis

class AutoModulePlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val autoModule = target.extensions.create("autoModule", AutoModule::class.java)
        logger.lifecycle("[Auto-Module] Starting")
        if (autoModule.ignored.isNotEmpty()) {
            logger.lifecycle("[Auto-Module] Ignoring modules: ${autoModule.ignored}")
        }
        target.gradle.settingsEvaluated {
            // need to evaluate the settings so it applies extension changes
            val timeTaken = measureTimeMillis {
                target.evaluateModules(autoModule)
            }
            logger.lifecycle("[Auto-Module] Generated modules graph in ${timeTaken}ms")
        }
    }

    private fun Settings.evaluateModules(autoModule: AutoModule) {
        val rootModule: ModuleNode = rootDir.toPath().rootModule(
            ignored = autoModule.ignored
        )

        if (rootModule.hasNoChildren()) {
            logger.lifecycle("[Auto-Module] No modules found in $rootDir")
        } else {
            rootModule.walk().forEach { module ->
                module.path?.let { path ->
                    logger.lifecycle("[Auto-Module] including $path")
                    include(path)
                }
            }
            rootModule.writeTo(
                directory = rootDir.toPath().resolve(autoModule.path),
                fileName = autoModule.modulesFileName
            )

            gradle.beforeProject { project ->
                project.extensions.add("local", GroovyRootModule(project.dependencies))
            }
        }
    }
}

private val logger: Logger by lazy { Logging.getLogger(Settings::class.java) }
