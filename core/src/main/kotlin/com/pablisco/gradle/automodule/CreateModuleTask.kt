package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.filetree.FileTreeScope
import com.pablisco.gradle.automodule.filetree.fileTree
import org.gradle.api.internal.AbstractTask
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

    private var templateDirectory: String? = null
    private var workingDirectory: String? = null

    @Option(option = "templateDirectory", description = "Name of the new module")
    fun templateDirectory(templateDirectory: String) {
        this.templateDirectory = templateDirectory
    }

    @Option(option = "workingDirectory", description = "Path of the new module. Root by default.")
    fun workingDirectory(workingDirectory: String) {
        this.workingDirectory = workingDirectory
    }

    @TaskAction
    fun createModule() {
        val templateDirectory = checkNotNull(templateDirectory) {
            """
            Must provide the name of the module as a command line parameter:
                ./gradlew $name --name={moduleName}
            """.trimIndent()
        }

        val workingDirectory = workingDirectory.asPath { defaultPath }
        workingDirectory
            .let { projectPath.resolve(it) }
            .fileTree {
                templateDirectory {
                    template.files(
                        ApplyTemplateScope(
                            fileTreeScope = this,
                            templateDirectory = templateDirectory,
                            workingDirectory = workingDirectory,
                            properties = project.properties.entries
                                .mapNotNull { (k, v) ->
                                    (v as? String)?.let { k to it }
                                }.toMap()
                        )
                    )
                }
            }
    }

}

@Suppress("unused") // API
class ApplyTemplateScope(
    fileTreeScope: FileTreeScope,
    val templateDirectory: String,
    val workingDirectory: Path,
    val properties: Map<String, String>
) : FileTreeScope by fileTreeScope

private fun String?.asPath(default: () -> Path): Path =
    this?.let { Paths.get(it) } ?: default()