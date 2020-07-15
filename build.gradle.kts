buildscript {
    repositories {
        maven(url = rootDir.resolve("repo"))
        jcenter()
        gradlePluginPortal()
    }
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
    }

    group = "com.pablisco.gradle.automodule"
    version = "0.12"
}

tasks {
    create<Delete>("clean") {
        delete(allprojects.map { it.buildDir })
        dependsOn("cleanAutoModule")
    }
}
