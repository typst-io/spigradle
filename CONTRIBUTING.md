# Contributing

## Classes

Please start all classes with the following copyright header:

```
/*
 * Copyright (c) $today.year Spigradle contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

If you use IntelliJ IDEA, the IDE can automatically append this header, as long as the [.idea](.idea) directory is shared in the GitHub repository.

## Terminology

[Included build](https://docs.gradle.org/current/userguide/composite_builds.html#included_build_declaring_substitutions): An included build is a separate Gradle build that is included into another build (forming a composite build).

[Version Catalog](https://docs.gradle.org/current/userguide/version_catalogs.html): libs.versions.toml with Kotlin DSL.

## Structures

- build-logic(internal plugins, included build)
  - catalog(io.typst.spigradle.catalog): Publishes a version catalog(libs.version.toml) for each module.
  - central-publish(io.typst.spigradle.central.publish): Publishes artifacts to Maven Central.
  - docs(spigradle-docs): Generate documentation with Dokka
  - java(spigradle-java): Centralizes the Java toolchain/version.
  - publish(spigradle-publish): Publishes plugins to the Gradle Plugin Portal.
- plugin: The Gradle plugins spigot, nukkit, bungee, ...
- spigot-catalog
- nukkit-catalog
- bungee-catalog
- common-catalog
- spigot-bom-1.16.5
- spigot-bom-1.20.1

## Versioning

root [gradle.properties](gradle.properties): The Gradle plugin, catalogs, boms

## Build

To build without running tests, execute the `assemble` task instead of `build`.

`./gradlew assemble`

## Manual testing

1. Run `./gradlew publishToMavenLocal` to publish Spigradle to your local repository.
2. In a Gradle project, apply Spigradle, and add `mavenLocal()` to `pluginManagement` in `settings.gradle`

```
// settings.gradle.kts
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "my-project"
```

```
// build.gradle.kts
plugins {
    id("io.typst.spigradle") version "the version published in the local repository"
}
```

3. Once you have set up the above, you can change the Spigradle code, run publishToMavenLocal, and then refresh Gradle in your test project.

## Markdown documents

To edit the docs, only modify the `template_*.md` files in [docs](docs).

After editing, execute the `updateTemplateDocs` task to update the docs. The task definition is in [spigradle-docs.gradle.kts](build-logic/docs/src/main/kotlin/spigradle-docs.gradle.kts)

Some special characters may need to be escaped, for example `$`. 

## Maintenance
The following must be updated consistently:
- ASM: [libs.version.toml#L6](gradle/libs.versions.toml)
- Minimum Java version: [SpigotPlugin.kt#L80](plugin/src/main/kotlin/io/typst/spigradle/spigot/SpigotPlugin.kt)
- Kotlin version: sync with Gradle embedded Kotlin version
- Hardcoded versions in [spigradle-docs.gradle.kts](build-logic/docs/src/main/kotlin/spigradle-docs.gradle.kts) (KOTLIN_VERSION, SHADOW_JAR_VERSION, IDEA_EXT_VERSION)
- Catalog versions: [gradle.properties](gradle.properties)