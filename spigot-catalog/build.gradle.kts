import io.typst.spigradle.catalog.Dependency
import io.typst.spigradle.catalog.SpigotDependencies

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.spigot.version")!!
description = "Spigot version catalog for Gradle"

spigradleCatalog {
    libraries.set(
        SpigotDependencies.entries.map { it.dependency }
                + Dependency(
            "io.typst.spigradle.spigot",
            "io.typst.spigradle.spigot.gradle.plugin",
            property("version")!!.toString(),
            "spigradleSpigot-plugin",
            versionRef = "spigradle",
            isLocal = true,
        )
    )
    plugins.set(
        listOf(
            Dependency(
                project.group.toString(),
                "spigradle.spigot",
                property("version")!!.toString(),
                "spigot",
                versionRef = "spigradle"
            )
        )
    )
}
