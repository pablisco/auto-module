package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.camelCase
import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.Path

internal class AutoModuleScope(private val delegate: Settings) : Settings by delegate {

    private val buildSrc: Path = rootDir.toPath().resolve("buildSrc")
    private val localBuild: Path = buildSrc.resolve("build/autoModule")
    internal val codeOutputDirectory: Path = buildSrc.resolve("src/main/kotlin/")
    internal val checkSumFile: File = localBuild.resolve("checksum").toFile()
    internal val cacheChecksum: String? = checkSumFile.takeIf { it.exists() }?.readText()

    val autoModule: AutoModule =
        extensions.create("autoModule", AutoModule::class.java)

    val rootModule: ModuleNode by lazy {
        rootDir.toPath().rootModule(
            ignored = autoModule.ignored,
            name = autoModule.entryPointName.camelCase()
        )
    }

}
