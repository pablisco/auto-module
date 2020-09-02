import com.pablisco.gradle.automodule.Versions
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType

/**
 * The versions set in settings by [com.pablisco.gradle.automodule.AutoModule.versions]
 */
@Suppress("unused") // Api
val ExtensionAware.versions: Versions get() = extensions.getByType()
