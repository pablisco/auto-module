package com.pablisco.gradle.automodule.utils

import org.gradle.api.initialization.Settings

/**
 * Adds an extension to [Settings] as well as every project.
 */
fun <T: Any> Settings.addGlobalExtension(name: String, extension: T) {
    extensions.add(name, extension)
    gradle.allprojects { extensions.add(name, extension) }
}