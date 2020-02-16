val libs = Libs
val build = Build
val tests = Tests

private const val kotlinVersion = "1.3.61"
private const val junitJupiterVersion = "5.6.0"

object Libs {

    const val kotlinJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    const val kotlinPoet = "com.squareup:kotlinpoet:1.5.0"

}

object Tests {

    const val junit5Jupiter = "org.junit.jupiter:junit-jupiter:$junitJupiterVersion"
    const val junit5JupiterApi = "org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion"
    const val junit5JupiterParams = "org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion"
    const val kluent = "org.amshove.kluent:kluent:1.60"

}

object Build {

    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val android = "com.android.tools.build:gradle:3.5.3"

}