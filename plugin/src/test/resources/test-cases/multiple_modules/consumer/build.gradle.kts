plugins { kotlin("jvm") }

dependencies {
    implementation(project(autoModules.libraryOne))
    implementation(project(autoModules.libraryTwo))
}