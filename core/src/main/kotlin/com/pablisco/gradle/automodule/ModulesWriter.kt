package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.camelCase
import com.pablisco.gradle.automodule.utils.snakeCase
import com.squareup.kotlinpoet.*
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import java.nio.file.Path

internal fun Collection<ModuleNode>.writeTo(directory: Path, fileName: String) {
    FileSpec.builder("", fileName).apply {
        addImport("org.gradle.kotlin.dsl", "project")
        addProperty(
            PropertySpec.builder("local", ClassName("", "Local"))
                .apply {
                    receiver(DependencyHandler::class.asClassName())
                    getter(
                        FunSpec.getterBuilder()
                            .addStatement("return Local(this)")
                            .build()
                    )
                }.build()
        )
        addType(cleanup().toTypeSpec("Local"))
        flatten().filterNot { it.children.isEmpty() }.forEach {
            addType(it.children.toTypeSpec(it.name))
        }
    }.build().writeTo(directory)
}

private fun ModuleNode.toPropertySpec() = when (children.size) {
    0 -> PropertySpec.builder(name.snakeCase(), Dependency::class.asClassName())
        .initializer("""dh.project("$path")""")
    else -> PropertySpec.builder(name.snakeCase(), ClassName.bestGuess(name.camelCase()))
        .initializer("""${name.camelCase()}(dh)""")
}.build()

private fun Collection<ModuleNode>.toTypeSpec(name: String) =
    TypeSpec.classBuilder(name.camelCase())
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(ParameterSpec("dh", DependencyHandler::class.asClassName()))
                .build()
        )
        .addProperties(map { it.toPropertySpec() })
        .build()

private fun Collection<ModuleNode>.flatten(): Sequence<ModuleNode> =
    fold(emptySequence()) { acc, it ->
        acc + it + it.children
    }
