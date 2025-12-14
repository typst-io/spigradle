import io.typst.spigradle.catalog.SpigotDependencies
import io.typst.spigradle.catalog.Dependency

plugins {
    java
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = providers.gradleProperty("catalog.spigot.version").get()
version = property("catalog.spigot.version")!!
description = "Spigot version catalog for Gradle"

spigradleCatalog {
    libraries.set(SpigotDependencies.entries.map { it.dependency })
    plugins.set(
        listOf(
            Dependency(
                project.group.toString(),
                "spigradle",
                property("spigradle.catalog.version")!!.toString(),
                "spigot",
                versionRef = "spigradle"
            )
        )
    )
}
