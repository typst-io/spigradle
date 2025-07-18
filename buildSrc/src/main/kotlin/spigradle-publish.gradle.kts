import io.typst.spigradle.build.VersionTask

plugins {
    id("com.gradle.plugin-publish")
    `maven-publish`
}

val spigradleVcsUrl = "https://github.com/spigradle/spigradle.git"

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
            artifact(spigradleDocsJar)
        }
    }
}

gradlePlugin {
    website = "https://github.com/spigradle/spigradle"
    vcsUrl = spigradleVcsUrl
    fun formatDesc(name: String) = "An intelligent Gradle plugin for developing $name plugin."

    plugins {
        create("spigradle") {
            displayName = "Spigradle Base Plugin"
            description = "The base plugin of Spigradle"
            id = "io.typst.spigradle.base"
            implementationClass = "io.typst.spigradle.SpigradlePlugin"
            tags = listOf("minecraft", "paper", "spigot", "bukkit", "bungeecord", "nukkit", "nukkitX")
        }
        create("spigot") {
            displayName = "Spigradle Spigot Plugin"
            description = formatDesc("Spigot")
            id = "io.typst.spigradle"
            implementationClass = "io.typst.spigradle.spigot.SpigotPlugin"
            tags = listOf("minecraft", "paper", "spigot", "bukkit")
        }
        create("bungee") {
            displayName = "Spigradle Bungeecord Plugin"
            description = formatDesc("Bungeecord")
            id = "io.typst.spigradle.bungee"
            implementationClass = "io.typst.spigradle.bungee.BungeePlugin"
            tags = listOf("minecraft", "bungeecord")
        }
        create("nukkit") {
            displayName = "Spigradle NukkitX Plugin"
            description = formatDesc("NukkitX")
            id = "io.typst.spigradle.nukkit"
            implementationClass = "io.typst.spigradle.nukkit.NukkitPlugin"
            tags = listOf("minecraft", "nukkit", "nukkitX")
        }
    }
}

tasks.register<VersionTask>("setVersion")