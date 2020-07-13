buildscript {
    repositories {
        jcenter()
        maven(url = rootDir.resolve("../../../../repo"))
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
    }
}
