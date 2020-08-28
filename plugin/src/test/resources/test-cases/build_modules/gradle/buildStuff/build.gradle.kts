//buildscript {
//    dependencies {
//        // TODO: Propagate versions from root
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")
//    }
//}

buildscript {
    repositories {
        jcenter()
    }
}

repositories {
    jcenter()
}

plugins { kotlin("jvm") version "1.4.0" }