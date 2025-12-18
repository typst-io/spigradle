import io.typst.spigradle.catalog.BungeeDependencies
import io.typst.spigradle.catalog.Dependency

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.bungee.version")!!
description = "BungeeCord version catalog for Gradle"

spigradleCatalog {
    libraries.set(
        BungeeDependencies.entries.map { it.dependency }
                + Dependency(
            "io.typst.spigradle.bungee",
            "io.typst.spigradle.bungee.gradle.plugin",
            property("version")!!.toString(),
            "spigradleBungee-plugin",
            versionRef = "spigradle",
            isLocal = true,
        )
    )
    plugins.set(
        listOf(
            Dependency(
                project.group.toString(),
                "spigradle.bungee",
                property("spigradle.catalog.version")!!.toString(),
                "bungee",
                versionRef = "spigradle"
            )
        )
    )
}