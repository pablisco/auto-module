package com.pablisco.gradle.automodule

import java.nio.file.Path
import java.nio.file.Paths

open class AutoModule(
    var path: Path = Paths.get("buildSrc/src/main/kotlin/"),
    var modulesFileName: String = "modules",
    var ignored: List<String> = emptyList(),
    var rootModuleName: String = "local"
) {

    fun ignore(vararg modules: String) {
        ignored += modules
    }

    fun path(path: Path) {
        this.path = path
    }

    fun modulesFileName(modulesFileName: String) {
        this.modulesFileName = modulesFileName
    }

    fun rootModuleName(rootModuleName: String) {
        this.rootModuleName = rootModuleName
    }

}
