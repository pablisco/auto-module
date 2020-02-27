package com.pablisco.gradle.automodule.gradle

import org.amshove.kluent.shouldBeEqualTo
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Path

internal fun Path.runGradleProjects(): BuildResult =
    runGradle {
        withArguments("projects", "--stacktrace")
    }

internal fun Path.runGradle(extras: GradleRunner.() -> GradleRunner = { this }): BuildResult =
    GradleRunner.create()
        .withProjectDir(this.toFile())
        .withPluginClasspath()
        .forwardOutput()
        .run(extras)
        .build()

internal fun BuildResult.shouldBeSuccess() =
    task(":projects")?.outcome shouldBeEqualTo TaskOutcome.SUCCESS