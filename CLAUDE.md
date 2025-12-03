# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spigradle is a Gradle plugin for developing Spigot, Bungeecord, and NukkitX plugins. It provides automatic plugin.yml/bungee.yml generation, main class detection, dependency shortcuts, repository shortcuts, and debug tasks.

## Build Commands

### Building and Testing
```bash
# Build the project
./gradlew build

# Run all tests (runs with 4 parallel forks)
./gradlew test

# Publish to local Maven repository
./gradlew publishToMavenLocal
```
**Important**: Tests depend on `publishToMavenLocal`, so the plugin is automatically published locally before running tests.

### Documentation
```bash
# Update documentation from templates
./gradlew updateTemplateDocs
```
This processes files in `docs/templates/` and `docs/root-templates/`, expanding variables and adding edit warnings to generated files.

### Version Management
```bash
# Set project version
./gradlew setVersion
```
Version is read from `version.txt` via `VersionTask.readVersion(project)`.

## Architecture

### Plugin IDs and Implementation Classes

The project publishes four Gradle plugins:

1. **`io.typst.spigradle.base`** → `io.typst.spigradle.SpigradlePlugin`
   - Base plugin providing repository and dependency shortcuts

2. **`io.typst.spigradle`** → `io.typst.spigradle.spigot.SpigotPlugin`
   - Main Spigot plugin with full functionality

3. **`io.typst.spigradle.bungee`** → `io.typst.spigradle.bungee.BungeePlugin`
   - Bungeecord plugin

4. **`io.typst.spigradle.nukkit`** → `io.typst.spigradle.nukkit.NukkitPlugin`
   - NukkitX plugin

### Core Components

#### Main Class Detection
- Uses ASM 9.8 to scan compiled bytecode
- Detects subclasses of `org/bukkit/plugin/java/JavaPlugin`, BungeeCord Plugin, or Nukkit Plugin
- Task: `SubclassDetection` in `src/main/kotlin/io/typst/spigradle/SubclassDetection.kt`
- Detection framework in `src/main/kotlin/io/typst/spigradle/detection/`
  - `ClassDefinition.kt` - Represents class metadata
  - `DetectionContext.kt` - Manages detection state
  - `DirectedGraph.kt` - Graph utilities for class hierarchy

#### YAML Generation
- Task: `YamlGenerate` in `src/main/kotlin/io/typst/spigradle/YamlGenerate.kt`
- Generates `plugin.yml`(for Spigot, Nukkit), `bungee.yml`
- Uses SnakeYAML Engine 2.9 for serialization
- Each extension provides `encodeToMap()` to convert configuration to YAML

#### Debug System (Spigot only)
- Downloads Paper/Spigot server automatically via `PaperDownloadTask`
- Creates IntelliJ IDEA run configurations using `gradle-idea-ext` plugin
- Tasks created:
  - `debug${projectName}` - Main debug task
  - `downloadPaper` - Downloads server JAR
  - `preparePluginDependencies` - Downloads plugin dependencies
  - `copyArtifactJar` - Copies plugin JAR to debug folder
  - `createJavaDebugScript` - Generates starter scripts for Windows/Unix
  - `cleanDebug${projectName}` - Cleans project debug folder
  - `cleanCache${platformName}` - Cleans global cache
- IDEA Run Configurations:
  - `Debug${projectName}` - Remote debug configuration (attaches to JVM debug port)
  - `Run${projectName}` - JarApplication run configuration (runs server JAR directly)
- Debug directory: `.gradle/spigradle-debug/${platform}`
- Global cache: `$GRADLE_USER_HOME/spigradle-debug-jars/`
- Debug Extension properties:
  - `jvmDebugPort` - JVM debug port (default: 5005)
  - `jvmArgs` - Custom JVM arguments
  - `programArgs` - Custom program arguments

#### Repository and Dependency Shortcuts
- Defined in:
  - `Repositories.kt` - Common repositories
  - `Dependencies.kt` - Common dependencies
  - `spigot/*Repositories.kt`, `spigot/*Dependencies.kt` - Spigot-specific
  - `bungee/*Repositories.kt`, `bungee/*Dependencies.kt` - Bungee-specific
  - `nukkit/*Repositories.kt`, `nukkit/*Dependencies.kt` - Nukkit-specific
- Groovy extensions registered via `setupGroovyExtensions()` to enable shorthand syntax

### Package Organization

- `io.typst.spigradle` - Core plugin, tasks, utilities
- `io.typst.spigradle.spigot` - Spigot extensions, tasks, models (SpigotExtension, Command, Permission, Load)
- `io.typst.spigradle.bungee` - Bungeecord extensions and models
- `io.typst.spigradle.nukkit` - NukkitX extensions and models
- `io.typst.spigradle.debug` - Debug infrastructure (DebugTask, DebugExtension, DebugRegistrationContext)
- `io.typst.spigradle.detection` - Class detection framework using ASM

### Build Configuration

#### BuildSrc Convention Plugins
Located in `buildSrc/src/main/kotlin/`:

- `spigradle-meta.gradle.kts` - Generates `SpigradleMeta.kt` with VERSION constant
- `spigradle-publish.gradle.kts` - Configures plugin publication with all four plugin IDs
- `spigradle-docs.gradle.kts` - Dokka configuration and `updateTemplateDocs` task

#### Key Dependencies (libs.versions.toml)
- Kotlin 2.0.21 (matched to Gradle's embedded Kotlin version)
- ASM 9.8 (bytecode manipulation)
- SnakeYAML Engine 2.9 (YAML generation)
- Gradle IDEA Ext 1.3 (IntelliJ run configurations)
- Gradle Download Task 5.6.0 (downloading Paper/Spigot JARs)
- Dokka 2.0.0 (API documentation)
- JUnit 5.12.1 (testing with Gradle TestKit)
- Spigot API 1.21.4-R0.1-SNAPSHOT (compile-only dependency)

#### Compatibility
- Gradle 8.0+ required
- Kotlin API/Language version: 1.8 (for Gradle 8.0+ compatibility)
- JVM Toolchain: Java 17

## Code Conventions

### Copyright Headers
All new source files must include:
```kotlin
/*
 * Copyright (c) 2025 Spigradle contributors.
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

### Markdown Documentation
- Do not edit generated files directly
- Edit templates in `docs/templates/` or `docs/root-templates/`
- Run `./gradlew updateTemplateDocs` after changes
- Generated files include edit warning comment

## Test Structure

Tests use Gradle TestKit for functional testing:
- Test resources in `src/test/resources/{platform}/{dsl}/`
  - `spigot/groovy/` - Groovy DSL test project
  - `spigot/kotlin/` - Kotlin DSL test project
  - Similar for bungee and nukkit
- Test classes in `src/test/kotlin/io/typst/spigradle/`
  - `SpigotGradleTest.kt`, `BungeeGradleTest.kt`, `NukkitGradleTest.kt`
  - `MainDetectionTest.kt`, `NewMainDetectionTest.kt`
  - `GenerateYamlTaskTest.kt`, `SpigotDebugTest.kt`

## Key Implementation Patterns

### Plugin Registration Flow
1. Platform plugins (Spigot/Bungee/Nukkit) call `applySpigradlePlugin()` to apply base plugin
2. Base plugin applies `java` plugin and registers Groovy extensions
3. Platform plugin calls `registerDescGenTask()` to set up YAML generation
4. YAML generation task depends on main class detection task
5. Debug tasks registered only for Spigot

### Extension Configuration
- Extensions use Gradle Property API for lazy evaluation
- `spigot { }`, `debugSpigot { }`, `bungee { }`, `nukkit { }` blocks in build files
- Configuration encoded to YAML via `encodeToMap()` methods

### Task Registration
- Uses `project.tasks.register()` for lazy task creation
- Task dependencies configured via `dependsOn()`
- Tasks grouped under "spigradle" or platform-specific groups
