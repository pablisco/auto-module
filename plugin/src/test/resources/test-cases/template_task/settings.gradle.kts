plugins {
    id("com.pablisco.gradle.automodule")
}

autoModule {
    template("default") {
        emptyFile( "build.gradle.kts")
    }
}
