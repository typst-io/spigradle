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

## Build

To build without running tests, execute the `assemble` task instead of `build`.

`./gradlew assemble`

## Versioning

The version is defined in [version.txt](version.txt). This makes it easy for third-party software to read the version.

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

After editing, execute the `updateTemplateDocs` task to update the docs. The task definition is in [spigradle-docs.gradle.kts](buildSrc/src/main/kotlin/spigradle-docs.gradle.kts)

Some special characters may need to be escaped, for example `$`. 

## Maintenance
The following must be updated consistently:
- [libs.version.toml#L6](gradle/libs.versions.toml)
- [SpigotPlugin.kt#L139](src/main/kotlin/io/typst/spigradle/spigot/SpigotPlugin.kt)
