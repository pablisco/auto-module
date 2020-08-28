rootProject.name = "auto-module"

apply(from = "gradle/include.settings.gradle.kts")

pluginManagement {
    repositories {
        // load self from local repo
        maven(url = rootDir.resolve("repo"))
        gradlePluginPortal()
    }
//    resolutionStrategy {
//        class Versions(private val propertiesFile: File) {
//
//            private val properties = java.util.Properties().apply {
//                propertiesFile.takeIf { it.exists() }?.also { load(it.reader()) }
//            }
//
//            private val keyValues = properties.mapNotNull { (k, v) ->
//                if (k is String && v is String) k to v else null
//            }
//
//            fun findDependency(group: String, name: String): String =
//                sequenceOf("${group}_${name}", name, group)
//                    .mapNotNull { properties[it] as? String }.firstOrNull()
//                    ?: error("no version present on $propertiesFile for plugin: ${group}:${name}")
//
//            fun findPluginVersion(id: String): String? =
//                keyValues.firstOrNull { (k, _) -> id.startsWith(k) }?.second
//
//        }
//        val versions = Versions(settingsDir.resolve("versions.properties"))
//        eachPlugin {
//            versions.findPluginVersion(requested.id.id)?.also { useVersion(it) }
//        }
//    }
}

plugins {
    id("com.pablisco.gradle.automodule") version "0.15"
}

autoModule {
    // Ignore tests cases and build folder
    ignore(":plugin:build")
    ignore(":demos")
    ignore(":gradle")
    ignore(":plugin:src:test:resources")
    ignore(":plugin:out")
    ignore(":plugin:src")

    pluginRepository(rootDir.resolve("repo"))
}

//gradle.allprojects {
//    val versions = Versions(settingsDir.resolve("versions.properties"))
//    configurations.all {
//        resolutionStrategy.eachDependency {
//            requested.takeUnless { it.hasVersion() }?.apply {
//                useVersion(versions.findDependency(group, name))
//            }
//        }
//    }
//}
//
//fun ModuleVersionSelector.hasVersion(): Boolean =
//    version?.takeIf { it.isNotEmpty() } != null
//
//private class Versions(private val propertiesFile: File) {
//
//    private val properties = java.util.Properties().apply {
//        propertiesFile.takeIf { it.exists() }?.also { load(it.reader()) }
//    }
//
//    private val keyValues = properties.mapNotNull { (k, v) ->
//        if (k is String && v is String) k to v else null
//    }
//
//    fun findDependency(group: String, name: String): String =
//        sequenceOf("${group}_${name}", name, group)
//            .mapNotNull { properties[it] as? String }.firstOrNull()
//            ?: error("no version present on $propertiesFile for plugin: ${group}:${name}")
//
//    fun findPluginVersion(id: String): String? =
//        keyValues.firstOrNull { (k, _) -> id.startsWith(k) }?.second
//
//}
