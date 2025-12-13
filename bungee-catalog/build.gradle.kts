import io.typst.spigradle.common.BungeeDependencies
import io.typst.spigradle.common.Dependencies
import io.typst.spigradle.common.Dependency

plugins {
    java
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = "1.0.0"
description = "BungeeCord version catalog for Gradle"

spigradleCatalog {
    val bungeeDeps = BungeeDependencies.entries.map { it.dependency }
    val commonDeps = Dependencies.entries.map { it.dependency }
    libraries.set(bungeeDeps + commonDeps)
    plugins.set(
        listOf(
            Dependency(
                project.group.toString(),
                "spigradle.bungee",
                rootProject.version.toString(),
                "bungee",
                versionRef = "spigradle"
            ),
            Dependency(
                "org.jetbrains",
                "gradle.plugin.idea-ext",
                "1.3",
                "ideaExt",
                versionRef = "ideaExt",
            )
        )
    )
}