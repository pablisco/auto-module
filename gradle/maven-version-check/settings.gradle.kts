rootProject.name = "maven-version-check"

includeBuild("../dependencies") {
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
