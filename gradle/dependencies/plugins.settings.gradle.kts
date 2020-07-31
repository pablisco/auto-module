pluginManagement {
    val versions = java.util.Properties().apply {
        file("src/main/resources/versions.properties").inputStream().use { load(it) }
    }
    resolutionStrategy {
        eachPlugin {
            val id = requested.id.id

            versions.forEach { (prefix, version) ->
                if (prefix is String && version is String && id.startsWith(prefix)) useVersion(version)
            }
        }
    }
}
