
val libs = Libs
val tests = Tests

object Libs {
    val kotlin = Kotlin
    val square = Square
    const val kotlinPoet = "com.squareup:kotlinpoet"

    object Square {
        const val retrofit = "com.squareup.retrofit2:retrofit"
        const val retrofitXml = "com.squareup.retrofit2:converter-jaxb"
    }

    object Kotlin {
        val jdk8 = kotlin("stdlib-jdk8")
        val io = kotlin("util-io")
        val x = X
        object X {
            val coroutines = kotlinx("coroutines-core")
        }
    }
}

object Tests {
    val junit5 = Junit5
    const val kluent = "org.amshove.kluent:kluent"

    object Junit5 {
        const val jupiter = "org.junit.jupiter:junit-jupiter"
        const val jupiterApi = "org.junit.jupiter:junit-jupiter-api"
        const val jupiterParams = "org.junit.jupiter:junit-jupiter-params"
    }

}

private fun kotlin(module: String) = "org.jetbrains.kotlin:kotlin-$module"
private fun kotlinx(module: String) = "org.jetbrains.kotlinx:kotlinx-$module"
