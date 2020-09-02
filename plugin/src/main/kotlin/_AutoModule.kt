import com.pablisco.gradle.automodule.AutoModule
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.getByType

/**
 * Extension to be used from scripts. Using default package to avoid imports.
 */
@Suppress("unused") // Api
fun Settings.autoModule(block: AutoModule.() -> Unit) {
    extensions.getByType<AutoModule>().block()
}
