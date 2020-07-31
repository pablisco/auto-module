includeBuild(file(".")) {
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
