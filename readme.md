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
- __[History](#history)__
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

Since modules are included with each sync, AutoModule will also add this new module to the graph and you are ready to use it straight away.

If you want to change the target directory where the module is to be created you can also add the `path` parameter in command line:

```bash
./gradlew createFeatureModule --templateDirectory=settings --workingDirectory=notFeature
```

The "body" of the `template` function is a lambda that has a receiver of type `ApplyTemplateScope`. This scope exposes a `FileTreeScope` (*by* delegation) as well as `templateDirectory`, `workingDirectory` and `properties`.

 - __Template Directory:__ The name of the folder where we are starting to write our template.
 - __Working Directory:__ The place the module is going to be written. By default this one is the root directory for the current project.
 - __Properties:__ These are the project's properties. These can be defined in the project's `gradle.properties` or the user one defined in `~/.gradle/gradle.properties`. Additionally you can include extra properties via command line like `-Pkey=value`.
 
The first two can be accessed directly inside the lambda. Properties is a simple Map<String, String> so we can access the values normally: `properties["package"]`

## History 

All this started with this Gist:

https://gist.github.com/pablisco/e50c792f1febb77af0fbc2d4f8f2810e

And this tweet:
 
https://twitter.com/pablisc0/status/1227933148622860289
 
Couple of weeks of sleepless evenings, and auto-module was born.

## License 

This project is licensed under the MIT License - see the [license.md](license.md) file for details
