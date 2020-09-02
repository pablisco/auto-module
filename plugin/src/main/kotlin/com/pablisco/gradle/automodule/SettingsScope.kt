package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.toGradlePath
import org.gradle.api.initialization.Settings
import java.nio.file.Path

internal class SettingsScope(
    val autoModule: AutoModule,
    settings: Settings
) : Settings by settings {

    private val workingDirectory = rootDir.toPath().resolve(".gradle/automodule")
    internal val graphModuleName = "${settings.rootProject.name}-modules"
    internal val generatedGraphModule = workingDirectory.resolve(graphModuleName)
    internal val generatedGraphSource = generatedGraphModule.resolve("src/main/kotlin/")
    internal val directoriesHashFile = workingDirectory.resolve("directories")
    internal val generatedMd5File = workingDirectory.resolve("generated")
    internal val versionFile = workingDirectory.resolve("version")
    internal val modulesFileName = "modules"

    val rootModule: ModuleNode by lazy {
        rootDir.toPath().rootModule(
            ignored = autoModule.ignored,
            name = rootProject.name
        )
    }

    val scriptsHash: Int
        get() = rootDir.toPath().scriptsHash(autoModule.ignored)

}

private fun Path.scriptsHash(ignored: List<String>) =
    findScripts()
        .map { relativize(parent).toGradlePath() }
        .filterNot { it in ignored }
        .hashCode()