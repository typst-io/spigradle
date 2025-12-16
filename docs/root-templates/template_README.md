# Spigradle

[![License](https://img.shields.io/badge/License-Apache_2.0-yellowgreen.svg)](https://github.com/typst-io/spigradle/blob/master/LICENSE)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.typst.spigradle)](https://plugins.gradle.org/plugin/io.typst.spigradle)
![TeamCity build status](https://ci.typst.io/app/rest/builds/buildType:id:Spigradle_Build/statusIcon.svg)

An intelligent Gradle plugin for developing plugins for Spigot, BungeeCord, and NukkitX.

[Migration Guide](docs/spigot_plugin.md#3x---2x) | [Chatbot Q&A](https://context7.com/typst-io/spigradle?tab=chat) | [Samples](https://github.com/spigradle/spigradle-sample)

## Features

- Auto-generate `plugin.yml` / `bungee.yml` with main class detection
- Debug task with server download and IDEA integration
- Repository and dependency shortcuts

## Quick Start

### settings.gradle.kts
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        // apply the version catalog to use the new dependency shortcut
        create("spigots") {
            from("io.typst:spigot-catalog:1.0.0")
            // if you need to override the version:
            // version("spigot", "1.21.1-R0.1-SNAPSHOT")
        }
    }
}

// ...
```

```kotlin
plugins {
    alias(spigots.plugins.spigot)
    alias(spigots.plugins.ideaExt) // optional, allows Spigradle generates Run Configurations for debug
}

dependencies {
    compileOnly(spigots.paper.api)
    compileOnly(spigots.protocolLib)
    compileOnly(spigots.vault) {
        transitive = false
    }
}

spigot {
    depend = listOf("ProtocolLib", "Vault")
    apiVersion = "1.21"
}

debugSpigot { // extension for debug\${ProjectName} task
    version = "1.21.8"
    eula = true
}
```

## Table of contents

- [Plugins](#plugins)
- [Requirements](#requirements)
- [Main Class Detection](#main-class-detection)
- [Repositories](#repositories)
- [Dependencies](#dependencies)
- [All docs](#all-docs)
- [Supporters](#supporters)

## Plugins

- **Spigot**: `id 'io.typst.spigradle'` - [Documentation](docs/spigot_plugin.md)
- **BungeeCord**: `id 'io.typst.spigradle.bungee'` - [Documentation](docs/bungeecord_plugin.md)
- **NukkitX**: `id 'io.typst.spigradle.nukkit'` - [Documentation](docs/nukkit_plugin.md)

## Requirements

All the plugins require Gradle 8.0+, the latest version is recommended.

To update your gradle wrapper:

```
gradlew wrapper --gradle-version \$GRADLE_VERSION --distribution-type all
```

## Main Class Detection

Spigradle automatically detects your plugin's main class using bytecode analysis with ASM (a Java bytecode manipulation framework). This eliminates the need to manually specify the main class in your build configuration.

### How it works

The detection process follows these steps:

1. **Bytecode Scanning**: Scans all compiled `.class` files using ASM's ClassReader
  - Uses optimized flags (`SKIP_CODE`, `SKIP_DEBUG`, `SKIP_FRAMES`) for faster processing
  - Only extracts class metadata (name, superclass, access modifiers)

2. **Class Hierarchy Building**: For each class file, extracts:
  - Class name (e.g., `com/example/MyPlugin`)
  - Superclass name (e.g., `org/bukkit/plugin/java/JavaPlugin`)
  - Access modifiers (public, abstract, final, etc.)

3. **Detection Context**: All discovered classes are registered in a directed graph structure
  - Maintains class inheritance relationships
  - Enables efficient traversal of the class hierarchy

4. **Main Class Resolution**: Traverses the inheritance graph to find a valid main class:
  - Must be a **non-abstract, public** class
  - Must directly or indirectly extend/implement the platform-specific base class
  - Prefers direct subclasses over deeper inheritance levels

5. **Result Integration**: The detected class name is automatically set as the `main` property in:
  - `plugin.yml` for Spigot/Nukkit
  - `bungee.yml` for BungeeCord

### Platform-specific detection targets

| Platform   | Detected Base Class                         | Task Name             |
|------------|---------------------------------------------|-----------------------|
| Spigot     | `org.bukkit.plugin.java.JavaPlugin`         | `detectSpigotMain`    |
| BungeeCord | `net.md_5.bungee.api.plugin.Plugin`         | `detectBungeeMain`    |
| NukkitX    | `cn.nukkit.plugin.PluginBase`               | `detectNukkitMain`    |

### Key features

- **Automatic**: No manual configuration required in most cases
- **Incremental**: Only scans changed `.class` files for faster builds
- **Gradle-native**: Fully integrated with Gradle's incremental compilation
- **Smart**: Understands complex inheritance hierarchies

### Manual override

If you need to manually specify the main class (e.g., multiple valid candidates), you can override the detection:

**Groovy:**
```groovy
spigot {
    main = 'com.example.MyCustomMain'
}
```

**Kotlin:**
```kotlin
spigot {
    main = "com.example.MyCustomMain"
}
```

### Technical details

For a contributor, internal usage:

- **Implementation**: [`SubclassDetection`](https://docs.typst.io/spigradle/$SPIGRADLE_VERSION/spigradle/io.typst.spigradle/-subclass-detection/index.html) task
- **Detection framework**: `io.typst.spigradle.detection` package
  - `ClassDefinition` - Represents class metadata
  - `DetectionContext` - Manages detection state and class graph
  - `DirectedGraph` - Graph utilities for class hierarchy traversal
- **Bytecode library**: ASM

## Repositories

Spigradle provides convenient shortcuts for adding Maven repositories commonly used in Minecraft plugin development. These repository shortcuts can be used in both Groovy and Kotlin DSL.

**Relations** column indicates which popular plugins/libraries are hosted in each repository.

### Repository shortcuts

Repository shortcuts are now available via platform-specific blocks: `spigotRepos {}`, `bungeeRepos {}`, `nukkitRepos {}`.

#### Spigot repositories (via `spigotRepos {}`)

| Name          | URL                                                            | Relations                               |
|---------------|----------------------------------------------------------------|-----------------------------------------|
| spigotmc()    | https://hub.spigotmc.org/nexus/content/repositories/snapshots/ | Spigot API                              |
| papermc()     | https://repo.papermc.io/repository/maven-public/               | Paper API                               |
| purpurmc()    | https://repo.purpurmc.org/snapshots                            | Purpur API                              |
| protocolLib() | https://repo.dmulloy2.net/nexus/repository/public/             | ProtocolLib                             |
| enginehub()   | https://maven.enginehub.org/repo/                              | worldguard, worldedit, commandhelper... |
| codemc()      | https://repo.codemc.org/repository/maven-public/               | bStats                                  |
| essentialsX() | https://repo.essentialsx.net/releases/                         | EssentialsX                             |
| frostcast()   | https://ci.frostcast.net/plugin/repository/everything          | BanManager                              |

#### Bungee repositories (via `bungeeRepos {}`)

| Name                 | URL                                                      | Relations                  |
|----------------------|----------------------------------------------------------|----------------------------|
| bungeecord()         | https://oss.sonatype.org/content/repositories/snapshots/ | BungeeCord API             |
| sonatype()           | https://oss.sonatype.org/content/repositories/snapshots/ | (alias for bungeecord)     |
| minecraftLibraries() | https://libraries.minecraft.net                          | Brigadier                  |

#### Nukkit repositories (via `nukkitRepos {}`)

| Name                 | URL                                          | Relations |
|----------------------|----------------------------------------------|-----------|
| nukkitX()            | https://repo.opencollab.dev/maven-snapshots  | NukkitX   |
| openCollabRelease()  | https://repo.opencollab.dev/maven-releases   |           |
| openCollabSnapshot() | https://repo.opencollab.dev/maven-snapshots  |           |

#### Common repositories (via `Repositories`)

| Name      | URL                                                      | Relations |
|-----------|----------------------------------------------------------|-----------|
| sonatype()| https://oss.sonatype.org/content/repositories/snapshots/ |           |
| jitpack() | https://jitpack.io                                       | Vault     |

### Groovy usage

```groovy
repositories {
    mavenCentral()
    spigotRepos {
        papermc()
        protocolLib()
    }
}
```

### Kotlin usage

```kotlin
repositories {
    mavenCentral()
    spigotRepos {
        papermc()
        protocolLib()
    }
}
```

## Dependencies

Spigradle provides shortcuts for common Minecraft plugin dependencies. Each shortcut automatically resolves the correct Maven coordinates and uses a sensible default version (which you can override).

**Important:** Make sure to add the corresponding repository (see [Repositories](#repositories)) before using these dependencies. The "Official repository" column indicates which repository function provides access to each dependency.

### Dependency shortcuts

Dependencies are now available via **Version Catalogs**. See [Quick Start](#quick-start) for setup.

| Catalog Alias     | Signature                                         | Default version      | Official repository       |
|-------------------|---------------------------------------------------|----------------------|---------------------------|
| spigot            | org.spigotmc:spigot-api:\$version                 | 1.21.8-R0.1-SNAPSHOT | spigotmc()                |
| spigotAll         | org.spigotmc:spigot:\$version                     | 1.21.8-R0.1-SNAPSHOT | mavenLocal(), BuildTools  |
| paper             | io.papermc.paper:paper-api:\$version              | 1.21.8-R0.1-SNAPSHOT | papermc()                 |
| purpur            | org.purpurmc.purpur:purpur-api:\$version          | 1.21.8-R0.1-SNAPSHOT | purpurmc()                |
| bungeecord        | net.md-5:bungeecord-api:\$version                 | 1.21-R0.4            | sonatype()                |
| minecraftServer   | org.spigotmc:minecraft-server:\$version           | 1.21.8-SNAPSHOT      | mavenLocal(), BuildTools  |
| bukkit            | org.bukkit:bukkit:\$version                       | 1.21.8-R0.1-SNAPSHOT | mavenLocal(), BuildTools  |
| craftbukkit       | org.bukkit:craftbukkit:\$version                  | 1.21.8-R0.1-SNAPSHOT | mavenLocal(), BuildTools  |
| protocolLib       | net.dmulloy2:ProtocolLib:\$version                | 5.4.0                | mavenCentral()            |
| vault             | com.github.MilkBowl:VaultAPI:\$version            | 1.7                  | jitpack()                 |
| luckperms         | net.luckperms:api:\$version                       | 5.5                  | mavenCentral()            |
| worldedit         | com.sk89q.worldedit:worldedit-bukkit:\$version    | 7.3.17               | enginehub()               |
| worldguard        | com.sk89q.worldguard:worldguard-bukkit:\$version  | 7.0.14               | enginehub()               |
| essentialsX       | net.essentialsx:EssentialsX:\$version             | 2.21.1               | essentialsX()             |
| banManager        | me.confuser.banmanager:BanManagerBukkit:\$version | 7.7.0-SNAPSHOT       | frostcast()               |
| commandHelper     | com.sk89q:commandhelper:\$version                 | 3.3.4-SNAPSHOT       | enginehub()               |
| bStats            | org.bstats:bstats-bukkit:\$version                | 3.0.2                | codemc()                  |
| mockBukkit        | org.mockbukkit.mockbukkit:mockbukkit-v1.21:\$ver  | 4.98.0               | mavenCentral()            |
| nukkit            | cn.nukkit:nukkit:\$version                        | 1.0-SNAPSHOT         | openCollabSnapshot()      |

### Groovy usage

```groovy
dependencies {
    compileOnly spigot("1.21.8") // or just spigot()
}
```

### Kotlin usage

```kotlin
import io.typst.spigradle.spigot.*

dependencies {
    compileOnly(spigot("1.21.8")) // or just spigot()
}
```

## All docs

- [Spigot Plugin](docs/spigot_plugin.md)
- [Bungeecord Plugin](docs/bungeecord_plugin.md)
- [Nukkit Plugin](docs/nukkit_plugin.md)
- [Multi-Module Projects Guide](docs/multimodule.md)

# Supporters

<a href="https://www.jetbrains.com/?from=Spigradle">
    <img src="assets/jetbrains.svg" alt="JetBrains OS License"/>
</a>
