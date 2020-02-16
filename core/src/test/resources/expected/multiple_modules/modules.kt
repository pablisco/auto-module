import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

val DependencyHandler.local: Local
    get() = Local(this)

class Local(dh: DependencyHandler) {
    val moduleTwo = dh.project(":moduleTwo")
    val moduleOne = dh.project(":moduleOne")
    val moduleThree = dh.project(":moduleThree")
}