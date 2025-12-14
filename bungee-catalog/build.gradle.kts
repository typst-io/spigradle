import io.typst.spigradle.catalog.BungeeDependencies
import io.typst.spigradle.catalog.Dependency

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.bungee.version")!!
description = "BungeeCord version catalog for Gradle"

spigradleCatalog {
    libraries.set(BungeeDependencies.entries.map { it.dependency })
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