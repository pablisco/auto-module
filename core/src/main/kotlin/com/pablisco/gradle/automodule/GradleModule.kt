package com.pablisco.gradle.automodule

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Entry point for [GroovyModule] instances.
 */
internal class GroovyRootModule(private val dh: DependencyHandler) {

    @Suppress("unused") // groovy dynamic property
    fun propertyMissing(property: String) =
        GroovyModule(dh, ":$property")
}

/**
 * Uses Groovy dynamic properties to define `project()` dependencies ina dynamic way.
 * This allows to use the same usage as we would have in Gradle Kotlin Scripts aiding
 * with future migrations.
 */
internal class GroovyModule(
    private val dh: DependencyHandler,
    private val path: String,
    dependency: Dependency = dh.project(mapOf("path" to path))
) : Dependency by dependency {

    @Suppress("unused") // groovy dynamic property
    fun propertyMissing(property: String) =
        GroovyModule(dh, "$path:$property")

}