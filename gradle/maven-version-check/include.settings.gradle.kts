includeBuild(file(".")) {
    dependencySubstitution {
        substitute(module("gradle:version-check")).with(project(":"))
    }
}

gradle.rootProject {
    buildscript {
        dependencies {
            classpath("gradle:version-check")
        }
    }
}
