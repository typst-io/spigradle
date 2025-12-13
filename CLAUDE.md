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

**Current Project Version:** 4.0.0 (from `version.txt`)

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
- **`spigot-catalog`**: Generates a Gradle Version Catalog with Spigot-related dependencies and Spigradle plugin coordinates. Published as `io.typst:spigot-catalog` (version 1.0.0, independently versioned). Configured via `SpigradleCatalogPlugin` from `build-logic/catalog/`. The catalog includes all Spigot dependencies from `SpigotDependencies` enum, common dependencies from `Dependencies` enum, and Spigradle plugin coordinates for easy version management.
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
- Uses ASM 9.9 to scan compiled bytecode with optimized flags (`SKIP_CODE`, `SKIP_DEBUG`, `SKIP_FRAMES`)
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
- Uses SnakeYAML Engine 3.0.1 for YAML serialization
- Each extension provides `encodeToMap()` to convert configuration to YAML

#### Debug System (Spigot only)
**Note:** The debug system uses a custom HTTP download implementation. See [HTTP Download Implementation](#http-download-implementation) for details.

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

#### HTTP Download Implementation
- Custom HTTP client using Java's built-in `java.net.http` package
- Implementation: `plugin/src/main/kotlin/io/typst/spigradle/HttpExtensions.kt`
- Key functions:
  - `fetchHttpGet(uri, handler)` - Generic HTTP GET with custom body handlers
  - `fetchHttpGetAsString(uri)` - Downloads as String (for API responses)
  - `fetchHttpGetAsByteArray(uri)` - Downloads as ByteArray (for JAR files)
- Configuration:
  - Follows redirects automatically (`HttpClient.Redirect.NORMAL`)
  - User-Agent: "spigradle"
  - Validates HTTP 2xx status codes, throws `IllegalStateException` on failure
- Used by:
  - `PaperDownloadTask` - Downloads Paper server JARs via PaperMC API
  - `PluginDependencyPrepareTask` - Downloads plugin dependencies from Maven repositories
- Paper download API: `https://fill.papermc.io/v3/projects/paper/versions/{version}/builds`

#### Repository and Dependency Shortcuts
- Common definitions in `common/src/main/kotlin/io/typst/spigradle/common/`:
  - `Repositories.kt` - Common repositories
  - `Dependencies.kt` - Common dependencies
  - `SpigotRepositories.kt`, `SpigotDependencies.kt` - Spigot-specific
  - `BungeeRepositories.kt` - Bungee-specific
  - `NukkitRepositories.kt`, `NukkitDependencies.kt` - Nukkit-specific
- Plugin-specific extensions in `plugin/src/main/kotlin/io/typst/spigradle/`:
  - `bungee/BungeeDependencies.kt` - Internal enum defining BungeeCord dependencies:
    - `BUNGEE_CORD`: net.md-5:bungeecord-api:1.21-R0.4
    - `BRIGADIER`: com.mojang:brigadier:1.0.18
    - Each entry wraps a `Dependency` object and provides `format(version)` method

### Package Organization

- `io.typst.spigradle.common` (common module) - Shared repository/dependency definitions
- `io.typst.spigradle` (plugin module) - Core plugin, tasks, utilities
- `io.typst.spigradle.spigot` - Spigot extensions, tasks, models (SpigotExtension, Command, Permission)
  - **Note on `load` property:** Documentation examples may show `Load.STARTUP` or `Load.POSTWORLD`, but `Load` is not an actual type in the codebase. The `load` property in `SpigotExtension` is of type `Property<String>`. Use string values directly: `load.set("STARTUP")` or `load.set("POSTWORLD")`.
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
- `catalog/` - Contains `SpigradleCatalogPlugin.kt` and `SpigradleCatalogExtension.kt`
  - Custom plugin for generating Gradle Version Catalogs
  - Applies `version-catalog` and `maven-publish` plugins
  - Extension: `spigradleCatalog { libraries.set(...); plugins.set(...) }`
  - Converts `Dependency` objects to version catalog format (aliases, coordinates, versions)
  - Used by `spigot-catalog` module to publish reusable catalogs

**Note on Catalog Plugins:**
- `build-logic/catalog/SpigradleCatalogPlugin.kt` - Convention plugin for building catalog modules (internal build-logic)
- `plugin/src/main/kotlin/.../spigot/SpigotCatalogPlugin.kt` - Published plugin for Spigot catalog functionality (external API)

These are distinct plugins with different purposes.

#### Key Dependencies (libs.versions.toml)
- Kotlin 2.2.20 (matched to Gradle's embedded Kotlin version)
- ASM 9.9 (bytecode manipulation for main class detection)
- SnakeYAML Engine 3.0.1 (YAML serialization for plugin.yml/bungee.yml generation)
- Gradle IDEA Ext 1.3 (IntelliJ IDEA run configurations)
- Gradle Plugin Publish 2.0.0 (plugin publication to Gradle Plugin Portal)
- Dokka 2.1.0 (API documentation generation)
- JUnit Jupiter 6.0.1 (testing with Gradle TestKit)
- Spigot API 1.21.8-R0.1-SNAPSHOT (default version, compile-only dependency)

#### Compatibility Requirements

**Build Environment:**
- Gradle: 9.2.1 (current wrapper), minimum 8.0+
- Kotlin API/Language version: 2.2 (using stdlib 2.2.20)
- JVM Toolchain: Java 17

**Runtime (Plugin Users):**
- Gradle 8.0+
- Java 17+ (for running Gradle builds)

**Target Platforms:**
- Spigot/Paper: 1.21.8+ (default API version 1.21.8-R0.1-SNAPSHOT)
- BungeeCord: 1.21-R0.4+
- NukkitX: Latest stable

## Working Rules

- When fixing code errors, always call `mcp__ide__getDiagnostics` first to get the exact error list before making changes.

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

#### Template Expansion System
The `updateTemplateDocs` task processes template files with variable substitution:

**Variables Expanded:**
- `${GRADLE_VERSION}` - Current Gradle version (dynamically from `gradle.gradleVersion`)
- `${SPIGRADLE_VERSION}` - Current Spigradle version (dynamically from `project.version`)
- `${KOTLIN_VERSION}` - Kotlin version (hardcoded: "2.2.20")
- `${SHADOW_JAR_VERSION}` - Shadow JAR plugin version (hardcoded: "9.2.2")
- `${IDEA_EXT_VERSION}` - IDEA Ext plugin version (hardcoded: "1.3")

**Processing Rules:**
1. Expands variables using Gradle's `expand()` function
2. Adds edit warning comment after each markdown heading (lines matching `^#[#]?[#]? `)
3. Renames files by removing `template_` prefix
4. Processes `docs/templates/*.md` → `docs/*.md`
5. Processes `docs/root-templates/*.md` → `*.md` (project root)

**Edit Warning Format:**
```markdown
[comment]: <> (!! Do not edit this file but 'docs/templates' or 'docs/root-templates', See [CONTRIBUTING.md] !!)
```

**Note:** Hardcoded versions in `build-logic/docs/src/main/kotlin/spigradle-docs.gradle.kts` should be kept in sync with `libs.versions.toml`.

## Test Structure

Tests use Gradle TestKit for functional testing:

### Test Resources
Located in `plugin/src/test/resources/{platform}/{dsl}/`:
- `spigot/groovy/` - Groovy DSL test project for Spigot
- `spigot/kotlin/` - Kotlin DSL test project for Spigot
- `bungee/groovy/` - Groovy DSL test project for BungeeCord
- `bungee/kotlin/` - Kotlin DSL test project for BungeeCord
- `nukkit/groovy/` - Groovy DSL test project for NukkitX
- `nukkit/kotlin/` - Kotlin DSL test project for NukkitX

### Test Classes
Located in `plugin/src/test/kotlin/io/typst/spigradle/`:

**Platform Integration Tests:**
- `spigot/SpigotGradleTest.kt` - Spigot plugin functionality tests
- `bungee/BungeeGradleTest.kt` - BungeeCord plugin functionality tests
- `nukkit/NukkitGradleTest.kt` - NukkitX plugin functionality tests

**Core Functionality Tests:**
- `MainDetectionTest.kt` - Legacy main class detection tests
- `NewMainDetectionTest.kt` - Refactored main class detection tests using new framework
- `GenerateYamlTaskTest.kt` - YAML generation task tests
- `spigot/SpigotDebugTest.kt` - Debug system tests (Spigot-specific)

**Utility and Integration Tests:**
- `DependencyResolutionTest.kt` - Dependency shortcut resolution tests
- `VersionModifierTest.kt` - Version string manipulation tests
- `BuildVersionInference.kt` - Build version inference logic tests
- `SpigotLibraryResolution.kt` - Spigot library resolution tests

**Test Base Class:**
- `GradleFunctionalTest.kt` - Abstract base class providing TestKit utilities

**Test Configuration:**
- Tests run with 4 parallel forks (configured in `plugin/build.gradle.kts`)
- Tests depend on `publishToMavenLocal` for plugin availability

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
- **Important:** The `load` property in `SpigotExtension`:
  - Type: `Property<String>` (not an enum)
  - Valid values: `"STARTUP"` or `"POSTWORLD"` (default)
  - Usage: `load.set("STARTUP")` (Kotlin DSL) or `load = "STARTUP"` (Groovy DSL)
  - Note: Documentation examples may show `Load.STARTUP`, but `Load` is not an actual type in the codebase

### Task Registration
- Uses `project.tasks.register()` for lazy task creation
- Task dependencies configured via `dependsOn()`
- Tasks grouped under "spigradle" or platform-specific groups