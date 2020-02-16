package com.pablisco.gradle.automodule.utils

import java.io.File

internal fun File.createFile(path: String? = null, content: String) {
    with(path?.let { File(this, it) } ?: this) {
        parentFile.mkdirs()
        createNewFile()
        writeText(content)
    }
}