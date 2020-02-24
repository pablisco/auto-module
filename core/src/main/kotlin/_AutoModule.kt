import com.pablisco.gradle.automodule.AutoModule
import org.gradle.api.plugins.ExtensionAware

/**
 * Extension to be used from scripts. Using default package to avoid imports.
 */
@Suppress("unused") // Api
fun ExtensionAware.autoModule(block: AutoModule.() -> Unit) {
    extensions.configure(AutoModule::class.java, block)
}