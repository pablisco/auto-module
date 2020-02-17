import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
    id("com.palantir.idea-test-fix") version "0.1.0"
    id("com.gradle.plugin-publish") version "0.10.1"
}

dependencies {
    implementation(libs.kotlinJdk8)
    implementation(libs.kotlinPoet)

    testImplementation(tests.junit5Jupiter)
    testImplementation(tests.junit5JupiterApi)
    testImplementation(tests.junit5JupiterParams)
    testImplementation(tests.kluent)
    testImplementation(gradleTestKit())
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }

    processTestResources.dependsOn(register<Copy>("copyTestResources") {
        from("${projectDir}/src/test/resources")
        into("${buildDir}/classes/kotlin/test")
    })
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    val localRepoPath = "${rootProject.rootDir}/repo"
    repositories {
        maven(url = uri(localRepoPath))
    }
    publications {
        create<MavenPublication>("maven") {
            logger.info("saving to $localRepoPath")
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

pluginBundle {
    website = "https://github.com/pablisco/auto-module/"
    vcsUrl = "https://github.com/pablisco/auto-module/"
    tags = listOf("auto", "module")
}

gradlePlugin {
    plugins {
        create("auto-module") {
            id = "com.pablisco.gradle.automodule"
            displayName = "Auto Module"
            description = "A Gradle plugin to generate the module graph and include the modules"
            implementationClass = "com.pablisco.gradle.automodule.AutoModulePlugin"
        }
    }
}
