# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spigradle is a Gradle plugin for developing Spigot, Bungeecord, and NukkitX plugins. It provides automatic plugin.yml/bungee.yml generation, main class detection, dependency shortcuts, repository shortcuts, and debug tasks.

## Build Commands

### Building and Testing
```bash
# Build the project
./gradlew assemble

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

## Project Structure

This is a multi-module Gradle project with composite builds:

```
spigradle/
├── plugin/                    # Main Gradle plugin module
│   └── src/main/kotlin/io/typst/spigradle/
├── spigot-catalog/            # Version Catalog for Spigot dependencies
├── common/                    # Composite build: shared dependencies/repositories
│   └── src/main/kotlin/io/typst/spigradle/common/
└── build-logic/               # Composite build: convention plugins
    ├── docs/                  # Dokka documentation configuration
    ├── publish/               # Plugin publication configuration
    ├── versioning/            # Version management
    └── catalog/               # Version Catalog generation plugin
```

### Module Descriptions

- **`plugin`**: Main Gradle plugin containing Spigot, Bungee, and Nukkit plugins. Published as `io.typst:spigradle`.
- **`spigot-catalog`**: Generates a Gradle Version Catalog with Spigot-related dependencies.
- **`common`**: Shared library (`io.typst:spigradle-common`) containing repository URLs, dependency coordinates, and extension interfaces. Can be used independently.
- **`build-logic`**: Internal convention plugins for building the project.

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
- Uses ASM 9.8 to scan compiled bytecode with optimized flags (`SKIP_CODE`, `SKIP_DEBUG`, `SKIP_FRAMES`)
- Task: `SubclassDetection` in `plugin/src/main/kotlin/io/typst/spigradle/SubclassDetection.kt`
- Detection targets by platform:
  - Spigot: `org/bukkit/plugin/java/JavaPlugin`
  - BungeeCord: `net/md_5/bungee/api/plugin/Plugin`
  - NukkitX: `cn/nukkit/plugin/PluginBase`
- Detection algorithm:
  1. Scans all `.class` files, extracts class name, superclass, interfaces, and modifiers
  2. Builds directed graph of inheritance relationships in `DetectionContext`
  3. Traverses graph to find classes that inherit from platform base class
  4. Filters to find non-abstract, public class as main class
  5. Writes result to output file in dot notation (e.g., `com.example.MyPlugin`)
- Detection framework in `plugin/src/main/kotlin/io/typst/spigradle/detection/`
  - `ClassDefinition.kt` - Represents class metadata (name, superclass, interfaces, modifiers)
  - `DetectionContext.kt` - Manages detection state and inheritance graph
  - `DirectedGraph.kt` - Graph utilities for class hierarchy traversal

#### YAML Generation
- Task: `YamlGenerate` in `plugin/src/main/kotlin/io/typst/spigradle/YamlGenerate.kt`
- Generates `plugin.yml`(for Spigot, Nukkit), `bungee.yml`
- Uses SnakeYAML Engine 2.9 for serialization
- Each extension provides `encodeToMap()` to convert configuration to YAML

#### Debug System (Spigot only)
- Downloads Paper/Spigot server automatically via `PaperDownloadTask`
- Creates IntelliJ IDEA run configurations using `gradle-idea-ext` plugin
- Tasks created:
  - `debug${projectName}` - Main debug task (launches server in new terminal)
  - `downloadPaper` - Downloads server JAR
  - `preparePluginDependencies` - Downloads plugin dependencies
  - `copyArtifactJar` - Copies plugin JAR to debug folder
  - `createJavaDebugScript` - Generates starter scripts for Windows/Unix
  - `cleanDebug${projectName}` - Cleans project debug folder
  - `cleanCache${platformName}` - Cleans global cache
- IDEA Run Configurations:
  - `Debug${projectName}` - Remote JVM Debug configuration (Recommended)
    - Lightweight: Server runs in separate terminal, IDE stays responsive
    - Workflow: Run `debug${projectName}` task first, then attach debugger
  - `Run${projectName}` - JarApplication run configuration
    - Heavy: Server process managed by IDE, increases memory usage
    - Use only when absolute convenience is needed
- Debug directory: `.gradle/spigradle-debug/${platform}`
- Global cache: `$GRADLE_USER_HOME/spigradle-debug-jars/`
- Debug Extension properties:
  - `jvmDebugPort` - JVM debug port (default: 5005)
  - `jvmArgs` - Custom JVM arguments
  - `programArgs` - Custom program arguments

#### Repository and Dependency Shortcuts
- Common definitions in `common/src/main/kotlin/io/typst/spigradle/common/`:
  - `Repositories.kt` - Common repositories
  - `Dependencies.kt` - Common dependencies
  - `SpigotRepositories.kt`, `SpigotDependencies.kt` - Spigot-specific
  - `BungeeRepositories.kt` - Bungee-specific
  - `NukkitRepositories.kt`, `NukkitDependencies.kt` - Nukkit-specific
- Plugin-specific extensions in `plugin/src/main/kotlin/io/typst/spigradle/`:
  - `bungee/BungeeDependencies.kt` - Bungee dependency extensions

### Package Organization

- `io.typst.spigradle.common` (common module) - Shared repository/dependency definitions
- `io.typst.spigradle` (plugin module) - Core plugin, tasks, utilities
- `io.typst.spigradle.spigot` - Spigot extensions, tasks, models (SpigotExtension, Command, Permission, Load)
- `io.typst.spigradle.bungee` - Bungeecord extensions and models
- `io.typst.spigradle.nukkit` - NukkitX extensions and models
- `io.typst.spigradle.debug` - Debug infrastructure (DebugTask, DebugExtension, DebugRegistrationContext)
- `io.typst.spigradle.detection` - Class detection framework using ASM

### Build Configuration

#### Build-Logic Convention Plugins
Located in `build-logic/`:

- `docs/` - Contains `spigradle-docs.gradle.kts`: Dokka configuration and `updateTemplateDocs` task
- `publish/` - Contains `spigradle-publish.gradle.kts`: Configures plugin publication with all four plugin IDs
- `versioning/` - Contains `spigradle-versioning.gradle.kts` and `VersionTask.kt`: Version management
- `catalog/` - Contains `SpigradleCatalogPlugin.kt`: Generates Gradle Version Catalogs

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
- Kotlin API/Language version: 2.2
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
- Key documentation files:
  - `docs/templates/template_spigot_plugin.md` - Spigot plugin guide
  - `docs/templates/template_bungeecord_plugin.md` - BungeeCord plugin guide
  - `docs/templates/template_nukkit_plugin.md` - NukkitX plugin guide
  - `docs/templates/template_multimodule.md` - Multi-module project guide
  - `docs/templates/template_README.md` - Main README template

## Test Structure

Tests use Gradle TestKit for functional testing:
- Test resources in `plugin/src/test/resources/{platform}/{dsl}/`
  - `spigot/groovy/` - Groovy DSL test project
  - `spigot/kotlin/` - Kotlin DSL test project
  - Similar for bungee and nukkit
- Test classes in `plugin/src/test/kotlin/io/typst/spigradle/`
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
- **Important:** `debugSpigot` is an EXTENSION (configuration block), NOT a task
  - The actual debug task is named `debug${ProjectName}` (e.g., `debugMyPlugin`)
  - `debugSpigot { }` configures the debug task, but is not itself executable

### Task Registration
- Uses `project.tasks.register()` for lazy task creation
- Task dependencies configured via `dependsOn()`
- Tasks grouped under "spigradle" or platform-specific groups