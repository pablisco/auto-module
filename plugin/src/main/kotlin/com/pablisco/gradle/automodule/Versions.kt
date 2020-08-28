@file:Suppress("FunctionName")

package com.pablisco.gradle.automodule

import com.pablisco.gradle.automodule.utils.castAs
import java.io.File
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import java.util.*

private fun Properties(f: Properties.() -> Unit) = Properties().apply(f)

class Versions internal constructor(private val rootDir: File, private val path: String) {

    private val properties by lazy {
        Properties {
            val local = rootDir.resolve(path)
            val remote = runCatching { URL(path) }.getOrNull()

            if (local.exists()) {
                load(local.reader())
            } else {
                remote?.openConnection()
                    ?.castAs<HttpURLConnection>()
                    ?.takeIf { it.responseCode == HTTP_OK }
                    ?.also { load(it.inputStream) }
            }
        }
    }

    private val keyValues = properties.mapNotNull { (k, v) ->
        if (k is String && v is String) k to v else null
    }

    fun findDependencyVersion(group: String, name: String): String =
        sequenceOf("${group}_${name}", name, group)
            .mapNotNull { properties[it] as? String }.firstOrNull()
            ?: error("no version present on $path for plugin: ${group}:${name}")

    fun findDependencyVersion(notation: String): String =
        notation.split(":").let { (group, name) -> findDependencyVersion(group, name) }

    fun findPluginVersion(id: String): String? =
        keyValues.firstOrNull { (k, _) -> id.startsWith(k) }?.second

}