package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.camelCase
import com.pablisco.gradle.automodule.utils.exists
import com.pablisco.gradle.automodule.utils.readText
import org.gradle.api.initialization.Settings
import java.nio.file.Path

internal class AutoModuleScope(private val delegate: Settings) : Settings by delegate {

    private val buildSrc: Path = rootDir.toPath().resolve("buildSrc")
    private val localBuild: Path = buildSrc.resolve("build/autoModule")
    internal val codeOutputDirectory: Path = buildSrc.resolve("src/main/kotlin/")
    internal val checkSumLocation: Path = localBuild.resolve("checksum")
    internal val cacheChecksum: String? get() = checkSumLocation.takeIf { it.exists() }?.readText()

    val autoModule: AutoModule =
        extensions.create("autoModule", AutoModule::class.java)

    val rootModule: ModuleNode by lazy {
        rootDir.toPath().rootModule(
            ignored = autoModule.ignored,
            name = autoModule.entryPointName.camelCase()
        )
    }

}
