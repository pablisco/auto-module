package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.camelCase
import com.pablisco.gradle.automodule.utils.exists
import com.pablisco.gradle.automodule.utils.readText
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.nio.file.Path

internal class AutoModuleScope(val settings: Settings) {

    private val buildSrc: Path = settings.rootDir.toPath().resolve("buildSrc")
    private val localBuild: Path = buildSrc.resolve("build/autoModule")
    internal val codeOutputDirectory: Path = buildSrc.resolve("src/main/kotlin/")
    internal val checkSumLocation: Path = localBuild.resolve("checksum")

    internal val cacheChecksum: String?
        get() = checkSumLocation.takeIf { it.exists() }?.readText()

    val autoModule: AutoModule =
        settings.extensions.create("autoModule", AutoModule::class.java)

    val rootModule: ModuleNode by lazy {
        settings.rootDir.toPath().rootModule(
            ignored = autoModule.ignored,
            name = autoModule.entryPointName.camelCase()
        )
    }

    val scriptsHash: Int
        get() = settings.rootDir.toPath().scriptsHash(autoModule.ignored)

}

internal fun AutoModuleScope.log(message: String) {
    logger.log(autoModule.logLevel, "[Auto-Module] $message")
}

private val logger: Logger by lazy { Logging.getLogger(AutoModuleScope::class.java) }

internal fun Path.scriptsHash(ignored: List<String>) =
    findScripts()
        .map { relativize(parent).toGradleCoordinates() }
        .filterNot { it in ignored }
        .hashCode()
