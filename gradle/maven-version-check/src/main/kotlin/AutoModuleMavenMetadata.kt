import kotlinx.coroutines.runBlocking

object AutoModuleMavenMetadata {

    private val api by lazy { MavenMetadataApi.gradlePlugins }

    val versions: List<String> by lazy {
        runBlocking {
            MavenMetadataApi.gradlePlugins
                .pluginMetadata(Plugin("com.pablisco.gradle.automodule"))
                .versioning.versions
        }
    }

}
