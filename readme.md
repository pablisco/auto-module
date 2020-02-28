# Auto Module

[![Actions](https://github.com/pablisco/auto-module/workflows/Publish/badge.svg)](https://github.com/pablisco/auto-module/actions) 
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/pablisco/gradle/automodule/core/maven-metadata.xml.svg?label=Gradle)](https://plugins.gradle.org/plugin/com.pablisco.gradle.automodule)

This Gradle plugin helps with module dependencies. And by help, it means it does the hard work for you.

## Why do I need this?

So, these are some (but not all) of the reasons why you should use this:

 - __Avoid user error:__ It's quite common, specially with complex projects, to have a large and nested project graph with all the available modules. Updating it can be tedious and, frankly, you probably rather be doing other things.
 - __Type safety:__ After you rename a module, any reference to it will complain when evaluating the project's build, so you know where you need to change your dependencies.
 - __IDE Auto-complete:__ One of the benefits of using `kts` build scripts is that the IDE can provide auto complete. This will save you multiple trips to the `settings.gradle[.kt]` script or the project structure to remember the name of the module you are looking for.
 - __Progressive upgrade:__ It has support for Groovy gradle so you don't have to migrate all your modules to use kts if you are not quite there yet or don't need it on all of them.
 - __Module generation:__ You can define tasks that can be used from command line to generate a predefined module structure with the provided template. The IDE only allows to create modules in the root of the project, this allows to create it on any directory as well as provide the means for templating how certain modules are defined.
 
## How do I use it?

Remove all your `include()` instructions inside `settings.gradle.[kts]` and add this:

```kotlin
plugins {
    id("com.pablisco.gradle.automodule") version "[latest.version]"
}
```

And that's it!

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

This plugin will create a `.kt` file inside `buildSrc` with the following code:

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

This will be accessible from any modules so you can add dependencies like:

```kotlin
implementation(local.features.home)
```

Note: The `buildSrc` is not generated, so for this plugin to work you will need to create
`buildSrc/build.gradle.kts` with the gradle dsl plugin enabled, like this:

```kotlin
repositories {
    jcenter()
}

plugins {
    `kotlin-dsl`
}
```
More details can be found on this example: [multi-kotlin-project-with-buildSrc](https://github.com/gradle/kotlin-dsl-samples/tree/master/samples/multi-kotlin-project-with-buildSrc)

Extra tip: Since `modules.kt` is generated each time the build is evaluated, it's possible to
add it to `.gitignore` to avoid unnecessary changes when committing :)

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

## Ignore modules

If you want to make sure a module *is not* included to the Gradle graph you can do it in two ways:

1. Adding the `.ignore` extension at the end of the `build.gradle[.kts]` script.
2. Inside `settings.gradle[.kts]` you can configure `autoModule` to do so:

```kotlin
autoModule {
  ignore(":modulePath", ":some:other:module")
}
```

## Generated files

By default, the generated code creates a file named `modules.kt` inside `buildSrc` with all the 
necessary details to add modules as dependencies.

If you want to change the name of this file you can do it inside `settings.gradle[.kts]`:

```kotlin
autoModule {
  modulesFileName = "AutoModules"
}
```

This means that, instead of generating a file called `modules.kt` it will generate one called 
`AutoModules.kt`.

## Custom root module name

By default, the root accessor property is named `local`. However, it's possible to define a custom name:

```kotlin
autoModule {
    rootModuleName = "banana"
}
```

This means that instead of using:

```kotlin
implementation(local.features.home)
```

you'll be able to use:

```kotlin
implementation(banana.features.home)
```

Note: Certain names (like "modules") are not allowed since we use `DependencyHandler`
to scope the root module and it already has a `modules` property defined as a JVM method 
with name `getModules()`. 

This type of scoping is used to avoid leaking everywhere in the build script.

## Legacy Groovy Script support

When you have a large project, it may not be possible to migrate all your scripts to Kotlin.
However, you can use the same semantics as you have in Kotlin with Groovy scripts:

```groovy
implementation(local.features.home)
```

This allows you to have a smooth migration to Kotlin Scripts in the future but remain with minimum 
changes in the mean time.

If you want to keep using Gradle with Groovy scripts, instead of Kotlin, this plugin will still
work. However, you still need to add support for kotlin scripts in `buildSrc/build.gradle`.

## History 

All this started with this Gist:

https://gist.github.com/pablisco/e50c792f1febb77af0fbc2d4f8f2810e

And this tweet:
 
https://twitter.com/pablisc0/status/1227933148622860289
 
Couple of weeks of sleepless evenings, and auto-module was born.

## License 

This project is licensed under the MIT License - see the [license.md](license.md) file for details
