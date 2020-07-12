package com.pablisco.gradle.automodule

/**
 * Uses Groovy dynamic properties to define `project()` dependencies in a dynamic way.
 * This allows to use the same usage as we would have in Gradle Kotlin Scripts aiding
 * with future migrations.
 */
class GroovyAutoModuleDependency(
    private val path: String
) : Map<String, String> by mapOf("path" to path) {

    @Suppress("unused") // groovy dynamic property
    fun propertyMissing(property: String) =
        GroovyAutoModuleDependency(path = "$path:$property")

}