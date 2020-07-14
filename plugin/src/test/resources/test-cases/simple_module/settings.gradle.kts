plugins {
    id("com.pablisco.gradle.automodule")
}

autoModule {
    pluginRepository(rootDir.resolve("../../../../repo").absolutePath)
}
