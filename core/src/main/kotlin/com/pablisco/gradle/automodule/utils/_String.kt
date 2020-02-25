package com.pablisco.gradle.automodule.utils

internal fun String.camelCase(): String =
    sanitize().split(' ').joinToString("") { it.capitalize() }

internal fun String.snakeCase(): String =
    sanitize().split(' ')
        .mapIndexed { index, s -> if (index == 0) s else s.capitalize() }
        .joinToString("")

private fun String.sanitize(replacement: Char = ' '): String =
    toCharArray().joinToString("") { c ->
        when {
            !c.isJavaIdentifierPart() -> replacement
            this[0] == c && !c.isJavaIdentifierStart() -> replacement
            else -> c
        }.toString()
    }