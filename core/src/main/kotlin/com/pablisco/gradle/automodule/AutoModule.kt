package com.pablisco.gradle.automodule

import org.gradle.api.logging.LogLevel
import java.nio.file.Path
import java.nio.file.Paths

open class AutoModule(
    var path: Path = Paths.get("buildSrc/src/main/kotlin/"),
    var modulesFileName: String = "modules",
    var ignored: List<String> = emptyList(),
    var rootModuleName: String = "local",
    var logLevel: LogLevel = LogLevel.LIFECYCLE
) {

    fun ignore(vararg modules: String) {
        ignored += modules
    }

}
