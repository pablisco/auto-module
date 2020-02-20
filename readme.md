# Auto Module

[![Actions](https://github.com/pablisco/auto-module/workflows/Publish/badge.svg)](https://github.com/pablisco/auto-module/actions) 
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/pablisco/gradle/automodule/core/maven-metadata.xml.svg?label=Gradle)](https://plugins.gradle.org/plugin/com.pablisco.gradle.automodule)



This Gradle plugin helps with module declaration. And by help, it means it does it all for you.

## How do I use it?

Remove all your `include()` calls inside `settings.gradle.[kts]` and add this:

```kotlin
plugins {
    id("com.pablisco.gradle.automodule") version "[latest.version]"
}
```

And that's it!

If you have a project with the following structure:

```
root
+-- app
\-- features
    +-- home
    \-- settings
```

A file inside `buildSrc` is be created with the following code:

```kotlin
val DependencyHandler.local: Local
    get() = Local(this)

class Local(
    dh: DependencyHandler
) {
    val app = dh.project(":app")
    val features = Features(dh)
    
    class Features(
        dh: dependencyHandler,
        dependency: Dependency = dh.project(":features")
    ) : Dependency by dependency  {
        val home = dh.project(":features:home")
        val settings = dh.project(":features:settings")
    }
}
```

This file will be accessible from any modules so you can add dependencies to other modules like:

```kotlin
implementation(local.features.home)
```

Note: This requires `buildSrc` to be enabled with the Kotlin dsl plugin as it is defined in this example: [multi-kotlin-project-with-buildSrc](https://github.com/gradle/kotlin-dsl-samples/tree/master/samples/multi-kotlin-project-with-buildSrc)

Extra tip: Since `modules.kt` is generated each time the build is evaluated, it's possible to add it to `.gitignore` to avoid unnecessary changes when committing :)

## Ignore modules

If you want to make sure a module is not added to the Gradle graph you can do it in two ways:

1. Adding the `.ignore` extension at the end of the `build.gradle[.kts]` script.
2. Inside `settings.gradle[.kts]` it's possible to configure `autoModule` to ignore a module:

```kotlin
autoModule {
  ignore(":modulePath", ":some:other:module")
}
```

## Generated files

By default, the generated code creates a file named `modules.kt` inside the `buildSrc` with all the necessary details to import local modules.

If you want to change the name of this file you can do it like this inside `settings.gradle[.kts]`:

```kotlin
autoModule {
  modulesFileName = "AutoModules"
}
```

This mans that, instead of generating a file called `modules.kt` it will generate one called `AutoModules.kt`.

## Custom root module name

By default, the root module is named `local`. However, it's possible to define a custom name:

```kotlin
autoModule {

    rootModuleName = "banana"

}
```

This means that instead of calling:

```kotlin
implementation(local.features.home)
```

you'll be able to call:

```kotlin
implementation(banana.features.home)
```

Note: The casing of the provided name will no be changed.

Another note: Certain names (like "modules") are not allowed since we use `DependencyHandler`
to namespace the root module and it already has a `modules` property defined as a Java method 
with name `getModules`. 

## Legacy Groovy Script support

When you have a large project, it may be possible to migrate all scripts to Kotlin.
It's possible to use the same semantics as we have in Kotlin in Groovy scripts:

```groovy
implementation(local.features.home)
```

This allows to have a smooth migration to Kotlin Scripts in the future but remain with minimum 
changes in the remaining scripts.
