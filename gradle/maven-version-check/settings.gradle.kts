rootProject.name = "maven-version-check"

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

includeBuild("../dependencies") {
    dependencySubstitution {
        substitute(module("gradle:dependencies:local")).with(project(":"))
    }
}
gradle.rootProject {
    buildscript {
        dependencies {
            classpath("gradle:dependencies:local")
        }
    }
}
