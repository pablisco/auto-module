plugins {
    kotlin("jvm")
    idea
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.palantir.idea-test-fix")
    id("com.gradle.plugin-publish")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-util-io")
    implementation("com.squareup:kotlinpoet")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.amshove.kluent:kluent")
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

//    "publishPlugins" {
//        onlyIf { version !in AutoModuleMavenMetadata.versions }
//    }
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.map { it.allSource })
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

afterEvaluate {
    publishing.publications.withType<MavenPublication>()
        .configureEach {
            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
        }
}

idea {
    module {
        // hides test kts files so they are not parsed by the IDE
        excludeDirs = setOf(file("src/test/resources/test-cases"))
    }
}
