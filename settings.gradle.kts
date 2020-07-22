rootProject.name = "auto-module"

pluginManagement {
    repositories {
        // load self from last build
        maven(url = rootDir.resolve("repo"))
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
    ignore(":plugin:build")
    ignore(":demos")
    ignore(":gradle")
    ignore(":plugin:src:test:resources")
    ignore(":plugin:out")

    pluginRepository(rootDir.resolve("repo"))
}
