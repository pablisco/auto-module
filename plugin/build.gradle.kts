plugins {
    kotlin("jvm") version kotlinVersion
    idea
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.palantir.idea-test-fix") version "0.1.0"
    id("com.gradle.plugin-publish") version "0.12.0"
}

dependencies {
    implementation(libs.kotlinJdk8)
    implementation(libs.kotlinIo)
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
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    val copyTestResources by registering(Copy::class) {
        from("${projectDir}/src/test/resources")
        into("${buildDir}/classes/kotlin/test")
    }

    processTestResources.configure {
        dependsOn(copyTestResources)
    }

    "publishPlugins" {
        onlyIf { version !in AutoModuleMavenMetadata.versions }
    }
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.map { it.allSource })
}

publishing {
    publications {
        create<MavenPublication>("Plugin") {
            from(components["java"])
            artifact(sourcesJar.get())
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"
        }
    }
    repositories {
        maven(url = rootDir.resolve("repo"))
    }
}

pluginBundle {
    website = "https://github.com/pablisco/auto-module/"
    vcsUrl = "https://github.com/pablisco/auto-module/"
    tags = listOf("automodule")
}

gradlePlugin {
    plugins {
        create("auto-module-plugin") {
            id = "com.pablisco.gradle.automodule"
            displayName = "Auto Module"
            description = "A Gradle plugin to generate the module graph and include the modules"
            implementationClass = "com.pablisco.gradle.automodule.AutoModulePlugin"
        }
    }
}

idea {
    module {
        // hides test kts files so they are not parsed by the IDE
        excludeDirs = setOf(file("src/test/resources/test-cases"))
    }
}
