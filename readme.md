# [WIP] Auto Module

This Gradle plugin helps with module declaration. And by help, it means it does it all for you.

## How do I use it?

Remove all your `include()` calls inside `settings.gradle.[kts]` and add this:

```kotlin
plugins {
    id("auto-module")
}
```

And that's it!

If you have a project with the following structure:

```
root
+-- app
+-- features
    +-- home
    +-- settings
```

A file inside `buildSrc` will be created with the following code:

```kotlin
val DependencyHandler.modules: Modules
    get() = Modules(this)

class Modules(dh: DependencyHandler) {
    val app = dh.project(":app")
    val features = Features(dh)
}

class Features(dh: dependencyHandler) {
    val home = dh.project(":features:home")
    val settings = dh.project(":features:settings")
}
```

This file will be accessible from any 

Note: This requires `buildSrc` to be enabled with the Kotlin dsl plugin as it is defined in this example: [multi-kotlin-project-with-buildSrc](https://github.com/gradle/kotlin-dsl-samples/tree/master/samples/multi-kotlin-project-with-buildSrc)
