plugins {
    id("com.pablisco.gradle.automodule")
}

autoModule {
    ignore(":settingsKtIgnored")
    pluginRepository(rootDir.resolve("../../../../repo").absolutePath)
}
