# Auto Module

[![Actions](https://github.com/pablisco/auto-module/workflows/Publish/badge.svg)](https://github.com/pablisco/auto-module/actions) 
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/pablisco/gradle/automodule/plugin/maven-metadata.xml.svg?label=Gradle)](https://plugins.gradle.org/plugin/com.pablisco.gradle.automodule)

This Gradle plugin helps with module dependencies. And by help, it means it does the hard work for you.

## Why do I need this?

So, these are some (but not all) of the reasons why you should use this:

 - __Avoid user error:__ It's quite common, specially with complex projects, to have a large and nested project graph with all the available modules. Updating it can be tedious and, frankly, you probably rather be doing other things.
 - __Type safety:__ After you rename a module, any reference to it will complain when evaluating the project's build, so you know where you need to change your dependencies.
 - __IDE Auto-complete:__ One of the benefits of using `kts` build scripts is that the IDE can provide auto complete. This will save you multiple trips to the `settings.gradle[.kt]` script or the project structure to remember the name of the module you are looking for.
 - __Progressive upgrade:__ It has support for Groovy gradle so you don't have to migrate all your modules to use kts if you are not quite there yet or don't need it on all of them.
 - __Module generation:__ You can define tasks that can be used from command line to generate a predefined module structure with the provided template. The IDE only allows to create modules in the root of the project, this allows to create it on any directory as well as provide the means for templating how certain modules are defined.
 

## Table of Contents 

<!-- toc -->
- __[How do I use it?](#how-do-i-use-it)__
- __[Ignore modules](#ignore-modules)__
- __[Legacy Groovy Script support](#legacy-groovy-script-support)__
- __[Module generation tasks](#module-generation-tasks)__
- __[Automatic Version Resolution](#automatic-version-resolution)__
  - __[By Group](#by-group)__
  - __[By Group and/or Name](#by-group-andor-name)__
- __[Self resolution, not possible](#self-resolution-not-possible)__
- __[Composite Builds](#composite-builds)__
- __[versions.properties for Build Modules](#versionsproperties-for-build-modules)__
- __[History](#history)__
- __[Local development](#local-development)__
- __[License](#license)__
<!-- /toc -->
 
## How do I use it?

Remove all your `include()` instructions inside `settings.gradle[.kts]` and add this:

```kotlin
plugins {
    id("com.pablisco.gradle.automodule") version "0.15"
}
```

That's it!

At least from the settings script sense. Now you will probably want to convert all the local project
dependencies to use the type safe graph!

If you have a project with the following structure:

```
root
+-- app
\-- features
    +-- home
    \-- settings
```

AutoModule generates a build module in `.gradle/automodule` with code similar to this:

```kotlin
object autoModules {
    val app = App
    object App : AutoModuleDependency by autoModuleDependency(":app")
    val features = Features
    object Features : AutoModuleDependency by autoModuleDependency(":features") {
        val home = Home
        object Home : AutoModuleDependency by autoModuleDependency(":features:home")
        object settings : AutoModuleDependency by autoModuleDependency(":features:settings")
    }
}
```

This will be accessible from any module, so you can add dependencies like:

```kotlin
implementation(project(autoModules.features.home))
```

## Ignore modules

If you want to make sure a module *is not* included to the Gradle graph you can do it in two ways:

1. Adding the `.ignore` extension at the end of the `build.gradle[.kts]` script.
2. Inside `settings.gradle[.kts]` you can configure `autoModule` to do so:

```kotlin
autoModule {
  ignore(":modulePath", ":some:other:module")
}
```

## Legacy Groovy Script support

When you have a large project, it may not be possible to migrate all your scripts to Kotlin.
However, you can use the same semantics as you have in Kotlin with Groovy scripts:

```groovy
implementation(autoModules.features.home)
```

This allows you to have a smooth migration to Kotlin Scripts in the future but remain with minimum 
changes in the meantime.

If you want to keep using Gradle with Groovy scripts, instead of Kotlin, this plugin will still
work. However, you still need to add support for kotlin scripts in `buildSrc/build.gradle`.

## Module generation tasks

You can define tasks that can be used from command line to generate new modules:

```kotlin
autoModule {
    template(
        path = Paths.get("features"), //optional, default is root
        type = "feature"
    ) { // this: ApplyTemplateScope
        file("build.gradle.kts", contents = """
            plugins { 
                kotlin("jvm") version kotlin_version 
            }

            name = "com.example.$templateDirectory"    

            dependencies {
                implementation(local.core)
            }
        """.trimIndent())
        "src" { 
            folder("main/kotlin")
            folder("test/kotlin")
        }   
    }   
}
```

This generates a task called `createFeatureModule` that you can use from command line like this:

```bash
./gradlew createFeatureModule --templateDirectory=settings
```

Calling that task will create a new module inside `$rootDir/features/settings/` with the defined files and directories.

AutoModule will pick up the new module and added it to the project as soon as any task or sync from the IDE is run

If you want to change the target directory where the module is to be created you can also add the `path` parameter in command line:

```bash
./gradlew createFeatureModule --templateDirectory=settings --workingDirectory=notFeature
```

The "body" of the `template` function is a lambda that has a receiver of type `ApplyTemplateScope`. This scope exposes a `FileTreeScope` (*by* delegation) as well as `templateDirectory`, `workingDirectory` and `properties`.

 - __Template Directory:__ The name of the folder where we are starting to write our template.
 - __Working Directory:__ The place the module is going to be written. By default, this one is the root directory for the current project.
 - __Properties:__ These are the project's properties. These can be defined in the project's `gradle.properties` or the user one defined in `~/.gradle/gradle.properties`. 
 Additionally, you can include extra properties via command line like `-Pkey=value`.
 
The first two values can be accessed directly inside the lambda. The last one, Properties, is a Map<String, String>, so we can access the values like: `properties["package"]`

## Automatic Version Resolution

You can add `versions.properties` to the root of the project. AutoModule will pick it up and use it to resolve dependencies and plugins.
There are 3 ways to define versions:

### By Group

Let say that we define versions.properties like this:
```properties
org.jetbrains.kotlin=1.4.0
org.jetbrains.kotlinx=1.3.9
```

This means that versions will be resolved when a dependency or plugin has `org.jetbrains.kotlin` or `org.jetbrains.kotlinx` as the group of the dependency or plugin. 

So, instead of applying the kotlin plugin like this:
```kotlin
plugins {
  kotlin("jvm") version "1.4.0"
}
```
You can do: 
```kotlin
plugins {
  kotlin("jvm")
}
```
This is because, underneath, `kotlin("xxx")` is mapped to `id("org.jetbrains.kotlin:kotlin-xxx")`.

If you are adding plugin dependencies though the buildScript closure in a project, the same applies. 
There is no longer a need to add versions anywhere you need to use plugins.

For dependencies is similar, you would normally add a library with the version at the end:

```kotlin
dependencues {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}
```

If you have defined `versions.properties` then you can omit the version:

```kotlin
dependencues {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}
```

AutoModule will resolve the version for you on all modules, so you only have to update it in one place: `versions.properties`.

### By Group and/or Name

It's quite common to have multiple artifacts belonging to the same group but different version.

For instance, you may have something like this:

```kotlin
dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.13.0")
}
```

Since both libraries have different versions, assigning a version to the group will lead to a Gradle error.It doesn't 
find the version of the artifact that you are telling it to get.

Here you have two options with `versions.properties`:

 - You can define the name instead of the group:
```properties
kotlinx-coroutines-core=1.3.9
kotlinx-serialization-runtime=0.13.0
```
 - Or, if you have libraries that clash on the name you can indicate both separated by an underscore:

```properties
org.jetbrains.kotlinx_kotlinx-coroutines-core=1.3.9
org.jetbrains.kotlinx_kotlinx-serialization-runtime=0.13.0
```

Additionally, it's possible to define a custom location for the versions file:

```kotlin
autoModule {
  versions = "gradle/versions.properties"
}
```

Or, you can even provide a remote `properties` file with an url:

```kotlin
autoModule {
  versions = "https://path.to/remote.properties"
}
```

Self resolution, not possible
----

For obvious, or not so obvious, reasons it's not possible to resolve the version of autoModule itself :)

## Composite Builds

The most common way to add logic or custom plugins to your build script is to use the `buildSrc` folder.

This is, in simple terms, a special project that is compiled before Gradle evaluates and runs the main build script.
It's quite common to use that to define versions values, specially when using Gradle kotlin DSL as it provide auto-complete.

The problem with `buildSrc` is that it doesn't use cache, and it's compiled and tested (if you have tests) every time
you run your build. You can read more about this on [this article](https://proandroiddev.com/stop-using-gradle-buildsrc-use-composite-builds-instead-3c38ac7a2ab3).
A better alternative is to use [composite builds](https://docs.gradle.org/current/userguide/composite_builds.html).
 
They are also run before the main build script, with the added benefit of using build cache and allowing to have multiple 
modules for different build features you may have on your code base. If you look at the source code of this plugin, you 
will see that we have two different modules, inside the `gradle` folder that we use for dependencies and to check the 
version of the current version before publishing.

The standard procedure with composite builds is to create a "root" project anywhere on the code base and add it inside `settings.gradle[.kts]`:

```kotlin
includeBuild("gradle/dependencies")
```

Then you would have to create a plugin class and make sure it's applied in one of the modules, in order to access the code defined on that build modules.
If you don't define and apply the plugin then the code will not be accessible.

With autoModule, things are a lot easier. If you create a build module inside the `gradle` folder, where you may have set 
up the gradle wrapper, then it'll get picked up and added to the build script. Quite similar to what happens with the normal modules.

On top of that, you no longer need to define a gradle plugin, which adds compile time on a clean build. The code will 
automatically be added to the classpath of the build script.

versions.properties for Build Modules
----

One more thing, if you have `versions.properties` defined on the root project, then you can also include it to 
the `settings.gradle[.kts]` of the build module:

```kotlin
plugins {
    id("com.pablisco.gradle.automodule") version "0.15"
}

autoModule {
    versions = "../../versions.properties"
}
```

This was a traditional problem of both `buildSrc` and composite builds because they defined versions on there, so they 
couldn't make use of them. AutoModule has you covered :)

## History 

All this started with this Gist:

https://gist.github.com/pablisco/e50c792f1febb77af0fbc2d4f8f2810e

As well as this tweet:
 
https://twitter.com/pablisc0/status/1227933148622860289
 
Couple of weeks of sleepless evenings, auto-module was born, and the rest is history... that I'm trying to continue to write

## Local development

If you want to run this project locally there are a few things to consider:

- If you want to use the current version (the one you are editing) of the plugin, you will have to deploy it to the 
local `repo` with the `publish` gradle task. This is why all the `settings.gradle.kts`, including the ones for the local 
plugins, have code to include `./repo` as a plugin repository. 

- More to come here :)

## License 

This project falls under the MIT License - see the [license.md](license.md) file for details
