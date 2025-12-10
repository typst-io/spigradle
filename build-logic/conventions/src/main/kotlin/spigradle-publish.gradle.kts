import io.typst.spigradle.buildlogic.VersionTask

plugins {
    id("com.gradle.plugin-publish")
    java
    `maven-publish`
}

val spigradleVcsUrl = "https://github.com/typst-io/spigradle.git"

val spigradleDocsJar by tasks.registering(Jar::class) {
    group = "spigradle build"
    archiveClassifier.set("docs")
    from("$projectDir/docs") {
        include("*.md")
    }
    from("$projectDir/CHANGELOG.md")
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "io.typst"
            artifactId = "spigradle"
            artifact(spigradleDocsJar)
        }
    }
}

gradlePlugin {
    website = "https://github.com/typst-io/spigradle"
    vcsUrl = spigradleVcsUrl
    fun formatDesc(name: String) = "An intelligent Gradle plugin for developing $name plugin."
//
    plugins {
        create("spigradle") {
            id = "io.typst.spigradle.base"
            displayName = "Spigradle Base Plugin"
            description = "The base plugin of Spigradle"
            tags = listOf("minecraft", "paper", "spigot", "bukkit", "bungeecord", "nukkit", "nukkitX")
            implementationClass = "io.typst.spigradle.SpigradlePlugin"
        }
        create("spigot") {
            id = "io.typst.spigradle"
            displayName = "Spigradle Spigot Plugin"
            description = formatDesc("Spigot")
            tags = listOf("minecraft", "paper", "spigot", "bukkit")
            implementationClass = "io.typst.spigradle.spigot.SpigotPlugin"
        }
        create("bungee") {
            id = "io.typst.spigradle.bungee"
            displayName = "Spigradle Bungeecord Plugin"
            description = formatDesc("Bungeecord")
            tags = listOf("minecraft", "bungeecord")
            implementationClass = "io.typst.spigradle.bungee.BungeePlugin"
        }
        create("nukkit") {
            id = "io.typst.spigradle.nukkit"
            displayName = "Spigradle NukkitX Plugin"
            description = formatDesc("NukkitX")
            tags = listOf("minecraft", "nukkit", "nukkitX")
            implementationClass = "io.typst.spigradle.nukkit.NukkitPlugin"
        }
    }
}

tasks.register<VersionTask>("setVersion")
