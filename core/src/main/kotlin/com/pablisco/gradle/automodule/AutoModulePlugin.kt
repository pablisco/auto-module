package com.pablisco.gradle.automodule

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.OutputFile
import java.io.File
import kotlin.system.measureTimeMillis

class AutoModulePlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        target.extensions.add("autoModule", AutoModuleExtension)
        logger.lifecycle("[Auto-Module] Starting")

        val modules = target.rootDir.lookupModules().toList()

        if (modules.isEmpty()) {
            logger.lifecycle("[Auto-Module] No modules found in ${target.rootDir}")
        } else {
            modules.allModules.forEach {
                logger.debug("[Auto-Module] including $it")
                target.include(it.path)
            }
            val timeTaken = measureTimeMillis {
                modules.cleanup().writeTo(
                    file = File(target.rootDir, AutoModuleExtension.output)
                )
            }
            logger.lifecycle("[Auto-Module] Generated modules graph in ${timeTaken}ms")
        }
    }
}


private val logger: Logger by lazy { Logging.getLogger(Settings::class.java) }

object AutoModuleExtension {

    @OutputFile
    var output = "buildSrc/src/main/kotlin/Modules.kt"
}
