val libs = Libs
val tests = Tests

const val kotlinVersion = "1.3.72"
const val junitJupiterVersion = "5.6.0"
const val retrofitVersion = "2.9.0"

object Libs {

    const val kotlinJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    const val kotlinIo = "org.jetbrains.kotlin:kotlin-util-io:$kotlinVersion"

    const val kotlinPoet = "com.squareup:kotlinpoet:1.5.0"

    const val kotlinxCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7"

    const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    const val retrofitXml = "com.squareup.retrofit2:converter-jaxb:$retrofitVersion"

}

object Tests {

    const val junit5Jupiter = "org.junit.jupiter:junit-jupiter:$junitJupiterVersion"
    const val junit5JupiterApi = "org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion"
    const val junit5JupiterParams = "org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion"
    const val kluent = "org.amshove.kluent:kluent:1.60"

}
