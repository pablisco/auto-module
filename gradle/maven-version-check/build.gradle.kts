plugins {
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    implementation(libs.kotlin.jdk8)
    implementation(libs.kotlin.x.coroutines)
    implementation(libs.square.retrofit)
    implementation(libs.square.retrofitXml)
}


tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf("-Xinline-classes")
        }
    }
}
