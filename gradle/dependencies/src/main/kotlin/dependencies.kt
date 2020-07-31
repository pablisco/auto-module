import java.util.*

val libs get() = Libs
val tests get() = Tests

object Libs {
    val kotlinJdk8 = resolve("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    val kotlinIo = resolve("org.jetbrains.kotlin:kotlin-util-io")
    val kotlinPoet = resolve("com.squareup:kotlinpoet")
    val kotlinxCoroutines = resolve("org.jetbrains.kotlinx:kotlinx-coroutines-core")//:1.3.7"
    val retrofit = resolve("com.squareup.retrofit2:retrofit")
    val retrofitXml = resolve("com.squareup.retrofit2:converter-jaxb")
}

object Tests {
    val junit5Jupiter = resolve("org.junit.jupiter:junit-jupiter")
    val junit5JupiterApi = resolve("org.junit.jupiter:junit-jupiter-api")
    val junit5JupiterParams = resolve("org.junit.jupiter:junit-jupiter-params")
    val kluent = resolve("org.amshove.kluent:kluent")
}

private object Versions : Properties() {
    init {
        load(javaClass.classLoader.getResourceAsStream("versions.properties"))
    }

    fun find(notation: String): String =
        entries.map { (k, v) -> (k as String).replace('_', ':') to (v as String) }
            .firstOrNull { (prefix, _) -> notation.startsWith(prefix) }
            ?.second
            ?: error("no version present on versions.properties for $notation")

}

private fun resolve(notation: String): String = notation.split(":").let { (group, artifact) ->
    "$group:$artifact:${Versions.find(notation)}"
}
