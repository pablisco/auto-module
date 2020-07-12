buildscript {
    repositories {
        maven(url = uri("$rootDir/repo"))
        google()
        jcenter()
        mavenCentral()
        gradlePluginPortal()
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

    group = "com.pablisco.gradle.automodule"
    version = "0.12"
}

tasks {
    create<Delete>("clean") {
        delete(allprojects.map { it.buildDir })
    }
}
