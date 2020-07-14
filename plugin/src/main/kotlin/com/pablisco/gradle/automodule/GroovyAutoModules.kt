package com.pablisco.gradle.automodule

/**
 * Used from Groovy Gradle scripts to access modules similarly to how they are accessed with Kotlin Gradle scripts.
 */
open class GroovyAutoModules {

    /**
     * Entry point for [GroovyAutoModuleDependency] instances.
     */
    @Suppress("unused") // groovy dynamic property
    fun propertyMissing(property: String): GroovyAutoModuleDependency =
        GroovyAutoModuleDependency(path =":$property")

}