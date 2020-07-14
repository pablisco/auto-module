import retrofit2.Retrofit
import retrofit2.converter.jaxb.JaxbConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import javax.xml.bind.annotation.*

class MavenMetadataApi internal constructor(
    repository: String,
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(repository)
        .addConverterFactory(JaxbConverterFactory.create())
        .build(),
    private val api: MavenApi = retrofit.create()
) : MavenApi by api {
    companion object {
        @Suppress("MemberVisibilityCanBePrivate") // Api
        fun withUrl(url: String): MavenMetadataApi = MavenMetadataApi(url)
        val gradlePlugins: MavenMetadataApi by lazy { withUrl("https://plugins.gradle.org/m2/") }
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
data class Metadata @JvmOverloads constructor(
    val groupId: String = "",
    val artifactId: String = "",
    val version: String = "",
    val versioning: Versioning = Versioning()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Versioning @JvmOverloads constructor(
    val latest: String = "",
    val release: String = "",
    @field:XmlElementWrapper(name = "versions")
    @field:XmlElement(name = "version")
    val versions: List<String> = mutableListOf(),
    val lastUpdated: Long = 0
)

interface MavenApi {

    @GET("{groupPath}/{artifactId}/maven-metadata.xml")
    suspend fun metadata(
        @Path("groupPath", encoded = true) groupPath: String,
        @Path("artifactId") artifact: Artifact
    ): Metadata

}

suspend fun MavenApi.metadata(group: Group, artifact: Artifact): Metadata =
    metadata(artifact = artifact, groupPath = group.id.replace(".", "/"))

suspend fun MavenApi.pluginMetadata(plugin: Plugin): Metadata =
    metadata(artifact = Artifact("${plugin.name}.gradle.plugin"), group = Group(plugin.name))

inline class Plugin(val name: String)
inline class Group(val id: String)
inline class Artifact(val id: String)