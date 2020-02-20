package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.camelCase
import org.gradle.api.Plugin
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberExtensionProperties
import kotlin.reflect.full.memberProperties

class AutoModulePlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val autoModule = target.extensions.create("autoModule", AutoModule::class.java)
        if (autoModule.ignored.isNotEmpty()) {
            autoModule.log("[Auto-Module] Ignoring modules: ${autoModule.ignored}")
        }
        // need to evaluate the settings so it applies user defined configuration
        target.gradle.settingsEvaluated {
            autoModule.checkRootNameForShadows()
            it.evaluateModules(autoModule)
        }
    }

    private fun Settings.evaluateModules(autoModule: AutoModule) {
        val rootModule: ModuleNode = rootDir.toPath().rootModule(
            ignored = autoModule.ignored,
            name = autoModule.rootModuleName.camelCase()
        )

        if (rootModule.hasNoChildren()) {
            autoModule.log("[Auto-Module] No modules found in $rootDir")
        } else {
            rootModule.walk().forEach { module ->
                module.path?.let { path ->
                    autoModule.log("[Auto-Module] including $path")
                    include(path)
                }
            }
            rootModule.writeTo(
                directory = rootDir.toPath().resolve(autoModule.path),
                fileName = autoModule.modulesFileName,
                rootModuleName = autoModule.rootModuleName
            )

            gradle.beforeProject { project ->
                project.extensions.add(
                    autoModule.rootModuleName,
                    GroovyRootModule(project.dependencies)
                )
            }
        }
    }

    private fun AutoModule.log(message: String) {
        logger.log(logLevel, message)
    }


}

private val logger: Logger by lazy { Logging.getLogger(AutoModulePlugin::class.java) }

/**
 * Can't use names like "modules" since [DependencyHandler] already has a function
 * called [DependencyHandler.getModules] which is represented as a property in Kotlin.
 */
private fun AutoModule.checkRootNameForShadows() {
    val type = DependencyHandler::class
    check(
        type.allProperties.none { it.name == rootModuleName } &&
                type.allJavaMethods.none { it.name == "get${rootModuleName.capitalize()}" }
    ) {
        "Can't use \"$rootModuleName\" as \"rootModuleName\" since " +
                "DependencyHandler already has that property"
    }
}

private val KClass<*>.allProperties: List<KCallable<*>>
    get() = declaredMemberProperties + memberProperties + memberExtensionProperties

private val KClass<*>.allJavaMethods: Array<Method>
    get() = java.methods + java.declaredMethods
