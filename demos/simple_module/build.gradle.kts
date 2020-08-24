// Enable for local testing
//buildscript {
//    repositories {
//        maven(url = rootDir.parentFile.parentFile.resolve("repo"))
//    }
//}

plugins {
    kotlin("jvm") version "1.4.0" apply false
}

allprojects {
    repositories {
        jcenter()
    }
}
