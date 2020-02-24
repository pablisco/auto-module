package com.pablisco.gradle.automodule.utils

import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberExtensionProperties
import kotlin.reflect.full.memberProperties

/**
 * Verifies there is no property on a given type to avoid unintentional shadowing.
 */
internal inline fun <reified T> checkPropertyIsNotPresentIn(
    propertyName: String,
    message: () -> String
) {
    check(T::class.doesNotHave(propertyName), message)
}

private fun KClass<*>.doesNotHave(propertyName: String) =
    allProperties.none { it.name == propertyName } and java.doesNotHave(propertyName)

private fun Class<*>.doesNotHave(propertyName: String) =
    allJavaMethods.none { it.name == "get${propertyName.capitalize()}" }

private val KClass<*>.allProperties: List<KCallable<*>>
    get() = declaredMemberProperties + memberProperties + memberExtensionProperties

private val Class<*>.allJavaMethods: Array<Method>
    get() = methods + declaredMethods