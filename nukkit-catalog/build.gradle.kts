import io.typst.spigradle.catalog.Dependency
import io.typst.spigradle.catalog.NukkitDependencies

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.nukkit.version")!!
description = "NukkitX version catalog for Gradle"

/*

    SPIGRADLOE_PLUGIN(
        Dependency(
            "io.typst.spigradle.nukkit",
            "io.typst.spigradle.nukkit.gradle.plugin",
            "4.0.0",
            "spigradleNukkit-plugin",
            versionRef = "spigradle",
            isLocal = true,
        )
    )
 */

spigradleCatalog {
    libraries.set(
        NukkitDependencies.entries.map { it.dependency }
                + Dependency(
            "io.typst.spigradle.nukkit",
            "io.typst.spigradle.nukkit.gradle.plugin",
            property("version")!!.toString(),
            "spigradleNukkit-plugin",
            versionRef = "spigradle",
            isLocal = true,
        )
    )
    plugins.set(
        listOf(
            Dependency(
                project.group.toString(),
                "spigradle.nukkit",
                property("version")!!.toString(),
                "nukkit",
                versionRef = "spigradle"
            )
        )
    )
}