plugins {
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.retrofit2:retrofit")
    implementation("com.squareup.retrofit2:converter-jaxb")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}


tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf("-Xinline-classes")
        }
    }
}
