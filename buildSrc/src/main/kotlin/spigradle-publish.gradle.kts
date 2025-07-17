import kr.entree.spigradle.build.VersionTask

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
            id = "kr.entree.spigradle.base"
            implementationClass = "kr.entree.spigradle.SpigradlePlugin"
            tags = listOf("minecraft", "paper", "spigot", "bukkit", "bungeecord", "nukkit", "nukkitX")
        }
        create("spigot") {
            displayName = "Spigradle Spigot Plugin"
            description = formatDesc("Spigot")
            id = "kr.entree.spigradle"
            implementationClass = "kr.entree.spigradle.spigot.SpigotPlugin"
            tags = listOf("minecraft", "paper", "spigot", "bukkit")
        }
        create("bungee") {
            displayName = "Spigradle Bungeecord Plugin"
            description = formatDesc("Bungeecord")
            id = "kr.entree.spigradle.bungee"
            implementationClass = "kr.entree.spigradle.bungee.BungeePlugin"
            tags = listOf("minecraft", "bungeecord")
        }
        create("nukkit") {
            displayName = "Spigradle NukkitX Plugin"
            description = formatDesc("NukkitX")
            id = "kr.entree.spigradle.nukkit"
            implementationClass = "kr.entree.spigradle.nukkit.NukkitPlugin"
            tags = listOf("minecraft", "nukkit", "nukkitX")
        }
    }
}

tasks.register<VersionTask>("setVersion")