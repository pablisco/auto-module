rootProject.name = "simple_module"

// Enable for local testing
//pluginManagement {
//    repositories {
//        maven(url = rootDir.resolve("../../repo"))
//        mavenCentral()
//    }
//}

plugins {
    id("com.pablisco.gradle.automodule") version "0.12"
}

// Enable for local testing
//autoModule {
//    pluginRepository(rootDir.resolve("../../repo").absolutePath)
//}
