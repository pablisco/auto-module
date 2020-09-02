rootProject.name = "dependencies"

pluginManagement {
    repositories {
        // load local repo
        maven(url = rootDir.resolve("../../repo"))
        gradlePluginPortal()
    }
}

plugins {
    id("com.pablisco.gradle.automodule") version "0.15"
}

autoModule {
    versions = "../../versions.properties"
    pluginRepository(rootDir.resolve("../../repo"))
}