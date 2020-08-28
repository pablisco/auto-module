package com.pablisco.gradle.automodule.utils

inline fun <reified A> Any.castAs(): A? = this as? A