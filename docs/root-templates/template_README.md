# Spigradle
[![License](https://img.shields.io/github/license/typst-io/spigradle.svg)](https://github.com/typst-io/spigradle/blob/master/LICENSE)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.typst.spigradle)](https://plugins.gradle.org/plugin/io.typst.spigradle)
[![TeamCity CI](https://ci.entree.dev/app/rest/builds/buildType(id:Spigradle_Build)/statusIcon)](https://ci.entree.dev/buildConfiguration/Spigradle_Build?branch=%3Cdefault%3E&buildTypeTab=overview&mode=builds&guest=1)

An intelligent Gradle plugin used to develop plugins for Spigot, Bungeecord and NukkitX.

# Benefits

- [Description file](https://entree.dev/spigradle/docs/spigradle/io.typst.spigradle.module.spigot/-spigot-extension/index.html) generation: `plugin.yml` and/or `bungee.yml`

- Main class detection

- Shortcuts for [repository](#repositories) and [dependency](#dependencies)

```groovy
plugins {
    id 'java'
    id 'io.typst.spigradle' version '$SPIGRADLE_VERSION'
}

group 'org.sample'
version '1.0.0'

repositories {
    mavenCentral()
    spigotmc()
}

dependencies {
    compileOnly spigot('1.21.8')
}

spigot {
    depends 'ProtocolLib'
    softDepends 'SomeLibrary'
    commands {
        create('mycmd') {
            aliases 'cmd'
        }
    }
}
```

# Table of contents

- [Plugins](#plugins)
  - [Spigot](#spigot)
  - [Bungeecord](#bungeecord)
  - [NukkitX](#nukkitx)
- [Requirements](#requirements)
- [Repositories](#repositories)
- [Dependencies](#dependencies)
- [See also](#see-also)
- [Supporters](#supporters)
- [The Spigot plugin](docs/spigot_plugin.md)
- [The Bungeecord plugin](docs/bungeecord_plugin.md)
- [The Nukkit plugin](docs/nukkit_plugin.md)
- [Sample](https://github.com/spigradle/spigradle-sample)

# Plugins

## Spigot

[Documentation](docs/spigot_plugin.md)

### Demo

- [Groovy - build.gradle](https://github.com/spigradle/spigradle-sample/tree/master/spigot/spigot.gradle)
- [Kotlin - build.gradle.kts](https://github.com/spigradle/spigradle-sample/tree/master/spigot-kotlin/spigot-kotlin.gradle.kts)

Groovy DSL

```groovy
plugins {
    id 'java'
    id 'io.typst.spigradle' version '$SPIGRADLE_VERSION'
}

repositories {
    mavenCentral()
    spigotmc()
}

dependencies {
    compileOnly spigot('1.21.8')
}

spigot {
  depends 'ProtocolLib'
  softDepends 'SomeLibrary'
  commands {
    create('mycmd') {
      aliases 'cmd'
    }
  }
  // if you want to exclude all [spigot.libraries]:
  // `excludeLibraries = ['*']`
}
```

<details>
<summary>Kotlin DSL</summary>

```kotlin
import io.typst.spigradle.spigot.*

plugins {
    kotlin("jvm") version "$KOTLIN_VERSION"
    id("io.typst.spigradle") version "$SPIGRADLE_VERSION"
}

repositories {
    mavenCentral()
    spigotmc()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(spigot("1.21.8"))
}

spigot {
  depends = listOf("ProtocolLib")
  softDepends = listOf("SomeLibrary")
  commands {
    create("mycmd") {
      aliases = listOf("cmd")
    }
  }
  // if you want to exclude all [spigot.libraries]:
  // `excludeLibraries = listOf("*")`
}
```

</details>

<details>
<summary>Groovy Legacy</summary>

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'kr.entree:spigradle:$SPIGRADLE_VERSION'
    }
}

apply plugin: 'io.typst.spigradle'
```

</details>

<details>
<summary>Kotlin Legacy</summary>

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("kr.entree:spigradle:$SPIGRADLE_VERSION")
    }
}

apply(plugin = "io.typst.spigradle")
```

</details>

## Bungeecord

[Documentation](docs/bungeecord_plugin.md)

### Demo
- [Groovy - build.gradle](https://github.com/spigradle/spigradle-sample/tree/master/bungeecord/bungeecord.gradle)
- [Kotlin - build.gradle.kts](https://github.com/spigradle/spigradle-sample/tree/master/bungeecord-kotlin/bungeecord-kotlin.gradle.kts)

Groovy DSL

```groovy
plugins {
    id 'java'
    id 'io.typst.spigradle.bungee' version '$SPIGRADLE_VERSION'
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly bungeecord('1.15')
}
```

<details>
<summary>Kotlin DSL</summary>

```kotlin
plugins {
    kotlin("jvm") version "$KOTLIN_VERSION"
    id("io.typst.spigradle.bungee") version "$SPIGRADLE_VERSION"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(bungeecord("1.15"))
}
```

</details>

<details>
<summary>Groovy Legacy</summary>

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'kr.entree:spigradle:$SPIGRADLE_VERSION'
    }
}

apply plugin: 'io.typst.spigradle.bungee'
```

</details>

<details>
<summary>Kotlin Legacy</summary>

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("kr.entree:spigradle:$SPIGRADLE_VERSION")
    }
}

apply(plugin = "io.typst.spigradle.bungee")
```

</details>

## NukkitX

[Documentation](docs/nukkit_plugin.md)

### Demo

- [Groovy - build.gradle](https://github.com/spigradle/spigradle-sample/tree/master/nukkit/nukkit.gradle)
- [Kotlin - build.gradle.kts](https://github.com/spigradle/spigradle-sample/tree/master/nukkit-kotlin/nukkit-kotlin.gradle.kts)

Groovy DSL

```groovy
plugins {
    id 'java'
    id 'io.typst.spigradle.nukkit' version '$SPIGRADLE_VERSION'
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly nukkit('1.0')
}
```

<details>
<summary>Kotlin DSL</summary>

```kotlin
plugins {
    kotlin("jvm") version "$KOTLIN_VERSION"
    id("io.typst.spigradle.nukkit") version "$SPIGRADLE_VERSION"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(bungeecord("1.15"))
}
```

</details>

<details>
<summary>Groovy Legacy</summary>

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'kr.entree:spigradle:$SPIGRADLE_VERSION'
    }
}

apply plugin: 'io.typst.spigradle.nukkit'
```

</details>

<details>
<summary>Kotlin Legacy</summary>

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("kr.entree:spigradle:$SPIGRADLE_VERSION")
    }
}

apply(plugin = "io.typst.spigradle.nukkit")
```

</details>

# Requirements

All the plugins require Gradle 8.0+, the latest version is recommended.

To update your gradle wrapper:

```
gradlew wrapper --gradle-version $GRADLE_VERSION --distribution-type all
```

# Repositories

|  Name         |  URL                                                           | Relations                               | Aliases       |
|---------------|----------------------------------------------------------------|-----------------------------------------|---------------|
| spigotmc()    | https://hub.spigotmc.org/nexus/content/repositories/snapshots/ |                                         | spigot()      |
| sonaytype()   | https://oss.sonatype.org/content/repositories/snapshots/       |                                         | bungeecord()  |
| papermc()     | https://papermc.io/repo/repository/maven-public/               |                                         | paper()       |
| jitpack()     | https://jitpack.io                                             | Vault                                   | vault()       |
| protocolLib() | https://repo.dmulloy2.net/nexus/repository/public/             |                                         |               |
| enginehub()   | https://maven.enginehub.org/repo/                              | worldguard, worldedit, commandhelper... |               |
| codemc()      | https://repo.codemc.org/repository/maven-public/               | BStats                                  | bStats()      |
| enderZone()   | https://ci.ender.zone/plugin/repository/everything/            | EssentialsX                             | essentialsX() |
| frostcast()   | https://ci.frostcast.net/plugin/repository/everything          | BanManager                              | banManager()  |
| nukkitX()     | https://repo.nukkitx.com/maven-snapshots                       | NukkitX                                 |               |

### Groovy usage

```groovy
repositories {
    engienhub()
}
```

### Kotiln usage

```kotlin
import io.typst.spigradle.kotlin.*

repositories {
    enginehub()
}
```

# Dependencies

|  Name             |  Signature                                       | Default version      | Official repository      |
|-------------------|--------------------------------------------------|----------------------|--------------------------|
| spigot(version)   | org.spigotmc:spigot-api:$version                 | 1.21.8-R0.1-SNAPSHOT | spigotmc()               |
| spigotAll()       | org.spigotmc:spigot:$version                     | 1.21.8-R0.1-SNAPSHOT | spigotmc()               |
| bungeecord()      | net.md-5:bungeecord-api:$version                 | 1.21-R0.4-SNAPSHOT   | spigotmc()               |
| minecraftServer() | org.spigotmc:minecraft-server:$version           | 1.21.8-SNAPSHOT      | mavenLocal(), BuildTools |
| paper()           | com.destroystokyo.paper:paper-api:$version       | 1.21.8-R0.1-SNAPSHOT | papermc()                |
| bukkit()          | org.bukkit:bukkit:$version                       | 1.21.8-R0.1-SNAPSHOT | mavenLocal(), BuildTools |
| craftbukkit()     | org.bukkit:craftbukkit:$version                  | 1.21.8-R0.1-SNAPSHOT | mavenLocal(), BuildTools |
| lombok()          | org.projectlombok:lombok:$version                | 1.18.38              | mavenCentral()           |
| spigradle()       | kr.entree:spigradle:$version                     | $SPIGRADLE_VERSION   | mavenCentral()           |
| protocolLib()     | com.comphenix.protocol:ProtocolLib:$version      | 5.3.0                | protocolLib()            |
| vault()           | com.github.MilkBowl:VaultAPI:$version            | 1.7                  | jitpack()                |
| vaultAll()        | com.github.MilkBowl:Vault:$version               | 1.7.3                | jitpack()                |
| luckPerms()       | me.lucko.luckperms:luckperms-api:$version        | 5.5.9                | mavenCentral()           |
| worldedit()       | com.sk89q.worldedit:worldedit-bukkit:$version    | 7.3.15               | enginehub()              |
| worldguard()      | com.sk89q.worldguard:worldguard-bukkit:$version  | 7.0.14               | enginehub()              |
| essentialsX()     | net.ess3:EssentialsX:$version                    | 2.21.1               | enderZone()              |
| banManager()      | me.confuser.banmanager:BanManagerBukkit:$version | 7.9.0-SNAPSHOT       | frostcast()              |
| commandhelper()   | com.sk89q:commandhelper:$version                 | 3.3.5-SNAPSHOT       | enginehub()              |
| bStats()          | org.bstats:bstats-bukkit:$version                | 3.0.2                | codemc()                 |
| nukkit            | cn.nukkit:nukkit:$version                        | 2.0.0-SNAPSHOT       | nukkitX()                |

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

# See also

- [The Spigot plugin](docs/spigot_plugin.md)
- [The Bungeecord plugin](docs/bungeecord_plugin.md)
- [The Nukkit plugin](docs/nukkit_plugin.md)
- [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html)

# Supporters

<a href="https://www.jetbrains.com/?from=Spigradle"> 
    <img src="assets/jetbrains.svg" alt="JetBrains OS License"/>
</a>
