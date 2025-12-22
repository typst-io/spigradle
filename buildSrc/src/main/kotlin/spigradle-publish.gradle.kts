import groovy.lang.GroovyObject
import kr.entree.spigradle.build.*
import java.util.*
import org.gradle.api.tasks.bundling.Jar

plugins {
    id("com.gradle.plugin-publish")
    id("com.eden.orchidPlugin")
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

val spigradleSourcesJar by tasks.registering(Jar::class) {
    group = "spigradle build"
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create("spigradle", MavenPublication::class) {
            from(components["java"])
            artifact(spigradleDocsJar.get())
            artifact(spigradleSourcesJar.get())
        }
    }
}

pluginBundle {
    website = "https://github.com/spigradle/spigradle"
    vcsUrl = spigradleVcsUrl
    tags = listOf("minecraft", "paper", "spigot", "bukkit", "bungeecord", "nukkit", "nukkitX")
    fun formatDesc(name: String) = "An intelligent Gradle plugin for developing $name plugin.\nNOTE: This plugin deprecated and changed the plugin id to 'io.typst.spigradle.spigot', new repository is here:\nhttps://github.com/typst-io/spigradle"
    plugins {
        create("spigradle") {
            displayName = "Spigradle Base Plugin"
            description = "The base plugin of Spigradle"
        }
        create("spigot") {
            displayName = "Spigradle Spigot Plugin"
            description = formatDesc("Spigot")
        }
        create("bungee") {
            displayName = "Spigradle Bungeecord Plugin"
            description = formatDesc("Bungeecord")
        }
        create("nukkit") {
            displayName = "Spigradle NukkitX Plugin"
            description = formatDesc("NukkitX")
        }
    }
}

tasks.register<VersionTask>("setVersion")