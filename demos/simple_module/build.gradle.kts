// Enable for local testing
//buildscript {
//    repositories {
//        maven(url = rootDir.parentFile.parentFile.resolve("repo"))
//    }
//}

plugins {
    kotlin("jvm") version "1.3.72" apply false
}

allprojects {
    repositories {
        jcenter()
    }
}
