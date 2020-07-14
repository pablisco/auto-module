rootProject.name = "auto-module"

pluginManagement {
    repositories {
        // load self from last build
        maven(url = "$rootDir/repo")
        gradlePluginPortal()
    }
}

fun includeBuilds(vararg names: String) {
    names.forEach { name ->
        includeBuild("$rootDir/gradle/$name") {
            dependencySubstitution {
                substitute(module("gradle:$name")).with(project(":"))
            }
        }
    }
    gradle.rootProject {
        buildscript {
            dependencies {
                names.forEach { name ->
                    classpath("gradle:$name")
                }
            }
        }
    }
}

includeBuilds("dependencies", "maven-version-check")

plugins {
    id("com.pablisco.gradle.automodule") version "0.12"
}

autoModule {
    // Ignore tests cases and build folder
    ignore(":core:build")
    ignore(":demos")
    ignore(":gradle")
    ignore(":core:src:test:resources")
    ignore(":core:out")

    pluginRepository("$rootDir/repo")
}
