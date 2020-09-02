plugins {
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jaxb:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}


tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf("-Xinline-classes")
        }
    }
}
