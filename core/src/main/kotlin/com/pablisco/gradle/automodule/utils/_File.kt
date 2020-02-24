package com.pablisco.gradle.automodule.utils

import java.io.File

internal fun File.createFile(
    content: String,
    path: String? = null
) {
    with(path?.let { File(this, it) } ?: this) {
        parentFile.mkdirs()
        createNewFile()
        writeText(content)
    }
}