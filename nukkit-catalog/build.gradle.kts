import io.typst.spigradle.catalog.NukkitDependencies
import io.typst.spigradle.catalog.Dependency

plugins {
    java
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.nukkit.version")!!
description = "NukkitX version catalog for Gradle"

spigradleCatalog {
    libraries.set(NukkitDependencies.entries.map { it.dependency })
    plugins.set(
        listOf(
            Dependency(
                project.group.toString(),
                "spigradle.nukkit",
                property("spigradle.catalog.version")!!.toString(),
                "nukkit",
                versionRef = "spigradle"
            )
        )
    )
}