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
        target.gradle.settingsEvaluated {
            val timeTaken = measureTimeMillis {
                target.evaluateModules(autoModule)
            }
            logger.lifecycle("[Auto-Module] Generated modules graph in ${timeTaken}ms")
        }
    }

    private fun Settings.evaluateModules(autoModule: AutoModule) {
        val modules = rootDir.lookupModules()
            .filterNot { it.path in autoModule.ignored }
            .toList()

        if (autoModule.ignored.isNotEmpty()) {
            logger.lifecycle("[Auto-Module] Ignoring modules: ${autoModule.ignored}")
        }

        if (modules.isEmpty()) {
            logger.lifecycle("[Auto-Module] No modules found in ${rootDir}")
        } else {
            modules.allModules.forEach {
                logger.lifecycle("[Auto-Module] including $it")
                include(it.path)
            }
            modules.cleanup().writeTo(
                directory = rootDir.toPath().resolve(autoModule.path),
                fileName = autoModule.modulesFileName
            )
        }
    }
}

private val logger: Logger by lazy { Logging.getLogger(Settings::class.java) }

