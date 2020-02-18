package com.pablisco.gradle.automodule

import java.nio.file.Path
import java.nio.file.Paths

open class AutoModule(
    var path: Path = Paths.get("buildSrc/src/main/kotlin/"),
    var modulesFileName: String = "modules",
    var ignored: List<String> = emptyList()
) {

    fun ignore(vararg modules: String) {
        ignored += modules
    }

}
