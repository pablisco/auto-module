import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
    id("com.palantir.idea-test-fix") version "0.1.0"
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