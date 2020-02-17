buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(build.android)
        classpath(build.kotlin)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

    group = "com.pablisco.gradle.automodule"
    version = "0.2"
}

tasks {
    create<Delete>("clean") {
        delete(allprojects.map { it.buildDir })
    }
}
