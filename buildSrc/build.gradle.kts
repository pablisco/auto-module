repositories {
    jcenter()
}

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.3.61"
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}