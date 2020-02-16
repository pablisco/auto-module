import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

val DependencyHandler.local: Local
    get() = Local(this)

class Local(dh: DependencyHandler) {
    val moduleOne = dh.project(":moduleOne")
    val parent = Parent(dh)
}

class Parent(dh: DependencyHandler) {
    val moduleTwo = dh.project(":parent:moduleTwo")
}