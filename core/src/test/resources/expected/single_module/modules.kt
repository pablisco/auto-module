import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

val DependencyHandler.local: Local
    get() = Local(this)

class Local(dh: DependencyHandler) {
    val singleModule = dh.project(":singleModule")
}