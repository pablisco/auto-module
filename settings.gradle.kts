rootProject.name = "auto-module"

pluginManagement {
    repositories {
        // load self from last build
        maven(url = uri("$rootDir/repo"))
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("$rootDir/gradle/dependencies") {
    dependencySubstitution {
        substitute(module("gradle:dependencies")).with(project(":"))
    }
}

gradle.rootProject {
    buildscript {
        dependencies {
            classpath("gradle:dependencies")
        }
    }
}

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
}
