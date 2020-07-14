plugins {
    kotlin("jvm")
}

dependencies {

    autoModules {
        implementation(project(library))
    }

    implementation(project(autoModules.library))

}
