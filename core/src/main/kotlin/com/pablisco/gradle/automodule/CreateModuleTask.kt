package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.filetree.fileTree
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

internal open class CreateModuleTask @Inject constructor(
    private val template: AutoModuleTemplate
) : AbstractTask() {

    private val projectPath = project.rootDir.toPath()
    private val defaultPath: Path = template.path.asPath { Paths.get("") }

    @get:Input
    @set:Option(option = "name", description = "Name of the new module")
    var moduleName: String? = null

    @get:Input
    @set:Option(option = "path", description = "Path of the new module. Root by default.")
    var modulePath: String? = null

    @TaskAction
    fun createModule() {
        val moduleDirectory = checkNotNull(moduleName) {
            """
            Must provide the name of the module as a command line parameter:
                ./gradlew $name --name={moduleName}
            """.trimIndent()
        }

        modulePath.asPath { defaultPath }
            .let { projectPath.resolve(it) }
            .fileTree {
                moduleDirectory {
                    template.files(this)
                }
            }
    }

}

private fun String?.asPath(default: () -> Path): Path =
    this?.let { Paths.get(it) } ?: default()