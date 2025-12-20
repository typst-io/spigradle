# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## TL;DR (What this repo is)
Spigradle is a Gradle plugin for developing Spigot, Bungeecord, and NukkitX plugins.
It provides:
- Automatic plugin.yml / bungee.yml generation
- Main class detection (ASM bytecode scan)
- Dependency and repository shortcuts (via catalogs and DSL)
- Debug tasks (Spigot only)

---

## Non-negotiables (DO / DON'T)

### DO
- Use the project’s Gradle workflows below (build/test/docs).
- Treat `debugSpigot { }` as an **extension**, not a task (task is `debug${ProjectName}`).
- For Markdown docs: edit templates, then run `updateTemplateDocs`.
- Add the required copyright header to **all new source files**.

### DON'T
- Do not edit generated Markdown files directly (`docs/*.md`, root generated docs).
- Do not assume `SpigotExtension.load` is an enum: it’s `Property<String>`.

---

## Quick commands (copy/paste)

### Build & test
```bash
# Build
./gradlew assemble

# Tests (runs with 8 parallel forks)
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
This processes:
- `docs/templates/` → `docs/*.md`
- `docs/root-templates/` → `*.md` (project root)

### Version management
Project version is defined in `gradle.properties`.

---

## Repo map (modules)

Multi-module Gradle project with composite builds:

```
spigradle/
├── plugin/                    # Main Gradle plugin module
│   └── src/main/kotlin/io/typst/spigradle/
├── spigot-catalog/            # Version Catalog for Spigot dependencies
├── bungee-catalog/            # Version Catalog for BungeeCord dependencies
├── nukkit-catalog/            # Version Catalog for NukkitX dependencies
├── common-catalog/            # Version Catalog for common dependencies
├── spigot-bom-1.16/           # BOM for Spigot 1.16 deps (placeholder)
├── spigot-bom-1.20/           # BOM for Spigot 1.20 deps (placeholder)
└── build-logic/               # Composite build: convention plugins
    ├── catalog/               # Version Catalog generation plugin
    ├── central-publish/       # Maven Central publication
    ├── docs/                  # Dokka documentation configuration
    ├── java/                  # Java toolchain configuration
    └── publish/               # Gradle Plugin Portal publication
```

### Module descriptions
- `plugin`: Main Gradle plugin (Spigot/Bungee/Nukkit). Published as `io.typst:spigradle`.
- `spigot-catalog`: Version Catalog for Spigot-related deps + Spigradle coords. Published as `io.typst:spigot-catalog`.
  - Configured via `SpigradleCatalogPublishPlugin` in `build-logic/catalog/`.
  - Includes all deps from `PaperDependencies` enum and Spigradle plugin coordinates.
- `bungee-catalog`: Published as `io.typst:bungee-catalog`.
- `nukkit-catalog`: Published as `io.typst:nukkit-catalog`.
- `common-catalog`: Common deps (Lombok, jOOQ, HikariCP, etc). Published as `io.typst:common-catalog`.
  - Includes deps from `CommonDependencies` enum.
- `spigot-bom-*`: Placeholder modules (not implemented yet).
- `build-logic`: Internal convention plugins for building this repo. Catalog versions are independently versioned in `gradle.properties`.

---

## Plugin IDs (public surface)

### Main plugins
1. `io.typst.spigradle.spigot` → `io.typst.spigradle.spigot.SpigotPlugin`
2. `io.typst.spigradle.bungee` → `io.typst.spigradle.bungee.BungeePlugin`
3. `io.typst.spigradle.nukkit` → `io.typst.spigradle.nukkit.NukkitPlugin`

### Base plugins (extensions/repository DSL only; no YAML generation)
4. `io.typst.spigradle.spigot-base` → `io.typst.spigradle.spigot.SpigotBasePlugin`
5. `io.typst.spigradle.bungee-base` → `io.typst.spigradle.bungee.BungeeBasePlugin`
6. `io.typst.spigradle.nukkit-base` → `io.typst.spigradle.nukkit.NukkitBasePlugin`

### Planned (not yet released)
- `io.typst.spigradle.paper` → `io.typst.spigradle.paper.PaperPlugin`
- `io.typst.spigradle.paper-base` → `io.typst.spigradle.paper.PaperBasePlugin`

---

## Core architecture (where to look)

### Main class detection (ASM)
- Task: `SubclassDetection`
  - `plugin/src/main/kotlin/io/typst/spigradle/SubclassDetection.kt`
- Uses ASM 9.9 with flags: `SKIP_CODE`, `SKIP_DEBUG`, `SKIP_FRAMES`
- Detection targets:
  - Spigot: `org/bukkit/plugin/java/JavaPlugin`
  - Bungee: `net/md_5/bungee/api/plugin/Plugin`
  - Nukkit: `cn/nukkit/plugin/PluginBase`

Detection algorithm:
1. Scan all `.class` files; read name/superclass/interfaces/modifiers
2. Build a directed inheritance graph in `DetectionContext`
3. Traverse to find classes inheriting from the platform base class
4. Filter to non-abstract, public class as main class
5. Write FQCN (dot notation) to output file

Detection framework:
- `plugin/src/main/kotlin/io/typst/spigradle/detection/`
  - `ClassDefinition.kt` - Class metadata
  - `DetectionContext.kt` - Detection state + graph
  - `DirectedGraph.kt` - Graph utilities

### YAML generation
- Task: `YamlGenerate`
  - `plugin/src/main/kotlin/io/typst/spigradle/YamlGenerate.kt`
- Generates:
  - `plugin.yml` (Spigot, Nukkit)
  - `bungee.yml` (Bungee)
- YAML engine: SnakeYAML Engine 3.0.1
- Extensions provide `encodeToMap()` to serialize to YAML

### Debug system (Spigot only)
- Downloads Paper/Spigot server automatically via `PaperDownloadTask`
- Creates IntelliJ IDEA run configs via `gradle-idea-ext`
- Debug dir: `.gradle/spigradle-debug/${platform}`
- Global cache: `$GRADLE_USER_HOME/spigradle-debug-jars/`

Tasks created:
- `debug${projectName}` - Main debug task (launches server in new terminal)
- `downloadPaper` - Download server JAR
- `preparePluginDependencies` - Download plugin deps
- `copyArtifactJar` - Copy plugin JAR to debug folder
- `createJavaDebugScript` - Generate starter scripts (Windows/Unix)
- `cleanDebug${projectName}` - Clean project debug folder
- `cleanCache${platformName}` - Clean global cache

IDEA run configurations:
- `Debug${projectName}` - Remote JVM Debug (recommended)
  - Run `debug${projectName}` first, then attach debugger
- `Run${projectName}` - JarApplication (heavy; IDE manages server process)

Debug extension properties:
- `jvmDebugPort` (default: 5005)
- `jvmArgs`
- `programArgs`

### HTTP download implementation
- Java built-in `java.net.http`
- `plugin/src/main/kotlin/io/typst/spigradle/HttpExtensions.kt`
- Key functions:
  - `fetchHttpGet(uri, handler)`
  - `fetchHttpGetAsString(uri)`
  - `fetchHttpGetAsByteArray(uri)`
- Config:
  - Redirect: `HttpClient.Redirect.NORMAL`
  - User-Agent: `"spigradle"`
  - Validates 2xx; throws `IllegalStateException` otherwise

Used by:
- `PaperDownloadTask` (PaperMC API)
- `PluginDependencyPrepareTask` (Maven downloads)

Paper download API:
- `https://fill.papermc.io/v3/projects/paper/versions/{version}/builds`

### Repository and dependency shortcuts
Dependency enums (`build-logic/catalog/src/main/kotlin/io/typst/spigradle/catalog/`):
- `CommonDependencies.kt`
- `PaperDependencies.kt`
- `BungeeDependencies.kt`
- `NukkitDependencies.kt`

Repository enums (`plugin/src/main/kotlin/io/typst/spigradle/`):
- `Repositories.kt`
- `paper/PaperRepositories.kt`
- `bungee/BungeeRepositories.kt`
- `nukkit/NukkitRepositories.kt`

---

## Package organization (high-level)
- `io.typst.spigradle.catalog` (build-logic) - Dependency enums + catalog generation
- `io.typst.spigradle` (plugin) - Core plugin/tasks/utilities
  - `PlatformPluginSpec.kt`
  - `ModuleRegistrationContext.kt`
  - `SubclassDetection.kt`
  - `YamlGenerate.kt`
- `io.typst.spigradle.spigot` - Spigot extensions/tasks/models
  - NOTE: `SpigotExtension.load` is `Property<String>`
    - Use `load.set("STARTUP")` or `load.set("POSTWORLD")`
- `io.typst.spigradle.paper` - Paper-specific (not yet released)
- `io.typst.spigradle.bungee` - Bungee extensions/models
- `io.typst.spigradle.nukkit` - Nukkit extensions/models
- `io.typst.spigradle.debug` - Debug infrastructure
- `io.typst.spigradle.detection` - ASM detection framework

---

## Build logic (convention plugins)

Located in `build-logic/`:
- `catalog/`
  - `SpigradleCatalogPublishPlugin.kt`
  - `SpigradleCatalogExtension.kt`
  - Applies `version-catalog` + `maven-publish`
  - DSL: `spigradleCatalog { libraries.set(...); plugins.set(...) }`
  - Used by catalog modules (`spigot-catalog`, `bungee-catalog`, etc.)

- `central-publish/` - `SpigradleCentralPublishPlugin.kt` (Maven Central)
- `docs/` - `spigradle-docs.gradle.kts` (Dokka + `updateTemplateDocs`)
- `java/` - `spigradle-java.gradle.kts` (toolchain/version)
- `publish/` - `spigradle-publish.gradle.kts` (Plugin Portal)

Note on catalog plugins:
- `build-logic/catalog/SpigradleCatalogPublishPlugin.kt` (internal convention plugin)
- `plugin/src/main/kotlin/.../spigot/SpigotCatalogPlugin.kt` (published external plugin)
  These are distinct plugins.

---

## Key versions (libs.versions.toml)
- Kotlin 2.2.20 (matches Gradle embedded Kotlin)
- ASM 9.9
- SnakeYAML Engine 3.0.1
- Gradle IDEA Ext 1.3
- Gradle Plugin Publish 2.0.0
- Dokka 2.1.0
- JUnit Jupiter 6.0.1
- Spigot API 1.21.10-R0.1-SNAPSHOT
- Commons Lang3 3.20.0, Commons Text 1.15.0

---

## Compatibility requirements

Build environment:
- Gradle: 9.2.1 wrapper (min 8.0+)
- Kotlin API/Language: 2.2 (stdlib 2.2.20)
- JVM toolchain: Java 17

Runtime (plugin users):
- Gradle 8.0+
- Java 17+

Target platforms:
- Spigot/Paper: 1.21.10+
- BungeeCord: 1.21-R0.4+
- NukkitX: Latest stable

---

## Working rules

- When fixing code errors, always call `mcp__ide__getDiagnostics` first to get the exact error list before making changes.

---

## Code conventions

### Copyright headers (required for new source files)
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

### Markdown documentation rules
- Do not edit generated files directly
- Edit templates in `docs/templates/` or `docs/root-templates/`
- Run `./gradlew updateTemplateDocs` after changes
- Generated files include an edit warning comment

Key template files:
- `docs/templates/template_spigot_plugin.md`
- `docs/templates/template_bungeecord_plugin.md`
- `docs/templates/template_nukkit_plugin.md`
- `docs/templates/template_multimodule.md`
- `docs/templates/template_README.md`

#### Template expansion system (`updateTemplateDocs`)
Variables expanded:
- `${GRADLE_VERSION}` - from `gradle.gradleVersion`
- `${SPIGRADLE_VERSION}` - from `project.version`
- `${KOTLIN_VERSION}` - hardcoded `"2.2.20"`
- `${SHADOW_JAR_VERSION}` - hardcoded `"9.2.2"`
- `${IDEA_EXT_VERSION}` - hardcoded `"1.3"`

Processing rules:
1. Expand variables using Gradle `expand()`
2. Add edit warning after each markdown heading (`^#[#]?[#]? `)
3. Rename files (remove `template_` prefix)
4. `docs/templates/*.md` → `docs/*.md`
5. `docs/root-templates/*.md` → `*.md`

Edit warning format:
```markdown
[comment]: <> (!! Do not edit this file but 'docs/templates' or 'docs/root-templates', See [CONTRIBUTING.md] !!)
```

Note: Hardcoded versions in `build-logic/docs/src/main/kotlin/spigradle-docs.gradle.kts`
should be kept in sync with `libs.versions.toml`.

---

## Tests (Gradle TestKit)

Test resources:
- `plugin/src/test/resources/{platform}/{dsl}/`
  - `spigot/{groovy,kotlin}/`
  - `bungee/{groovy,kotlin}/`
  - `nukkit/{groovy,kotlin}/`

Test classes (`plugin/src/test/kotlin/io/typst/spigradle/`):
Platform integration:
- `spigot/SpigotGradleTest.kt`
- `bungee/BungeeGradleTest.kt`
- `nukkit/NukkitGradleTest.kt`

Core:
- `MainDetectionTest.kt`
- `NewMainDetectionTest.kt`
- `GenerateYamlTaskTest.kt`
- `spigot/SpigotDebugTest.kt`

Utilities / integration:
- `DependencyResolutionTest.kt`
- `VersionModifierTest.kt`
- `BuildVersionInference.kt`
- `SpigotLibraryResolution.kt`

Base:
- `GradleFunctionalTest.kt`

Test configuration:
- Tests run with 8 parallel forks (`plugin/build.gradle.kts`)
- Tests depend on catalog `publishToMavenLocal` for plugin availability

---

## Key implementation patterns (mental model)

### Plugin registration flow
1. Platform plugin (Spigot/Bungee/Nukkit) applies `java-base` and corresponding base plugin
2. Base plugin registers extensions and repository DSL
3. Platform plugin creates `ModuleRegistrationContext` via `PlatformPluginSpec`
4. Platform plugin calls `registerDescGenTask()` to set up YAML generation
5. YAML generation depends on main class detection task
6. Debug tasks registered via `PaperDebugTask.register()` (Spigot only)

### Extension configuration
- Uses Gradle Property API (lazy evaluation)
- Blocks in build scripts: `spigot { }`, `debugSpigot { }`, `bungee { }`, `nukkit { }`
- Configuration is serialized via `encodeToMap()`

Important:
- `debugSpigot` is an **EXTENSION**, not a task.
  - Task is `debug${ProjectName}` (e.g., `debugMyPlugin`)
- `SpigotExtension.load`:
  - `Property<String>` (not enum)
  - Values: `"STARTUP"` or `"POSTWORLD"` (default)
  - Kotlin DSL: `load.set("STARTUP")`
  - Groovy DSL: `load = "STARTUP"`
  - Docs may show `Load.STARTUP`, but `Load` is not a type in this codebase

### Task registration
- Uses `project.tasks.register()` (lazy)
- Dependencies via `dependsOn()`
- Group under `"spigradle"` or platform-specific groups
