plugins {
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.retrofit)
    implementation(libs.retrofitXml)
    implementation(libs.kotlinxCoroutines)
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf("-Xinline-classes")
        }
    }
}
