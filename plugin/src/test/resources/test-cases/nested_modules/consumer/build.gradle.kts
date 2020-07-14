plugins { kotlin("jvm") }

dependencies {
    implementation(project(autoModules.library))
    implementation(project(autoModules.group.library))
    implementation(project(autoModules.group.library.nested))
}
