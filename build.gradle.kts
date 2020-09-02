buildscript {
    repositories {
        maven(url = rootDir.resolve("repo"))
        jcenter()
        gradlePluginPortal()
    }
}

plugins {
    id("com.github.blueboxware.tocme")
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
    }
    group = "com.pablisco.gradle.automodule"
    version = "0.15"
}

tasks {
    create<Delete>("clean") {
        delete(allprojects.map { it.buildDir })
        dependsOn("cleanAutoModule")
    }
}

tocme {
    doc(file("readme.md"))
}
