import io.typst.spigradle.catalog.NukkitDependencies
import io.typst.spigradle.catalog.PluginDependency

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.nukkit.version")!!
description = "NukkitX version catalog for Gradle"

spigradleCatalog {
    val nukkitPlugins = listOf(
        PluginDependency(
            "${project.group}.spigradle.nukkit",
            property("spigradle.version")!!.toString(),
            "nukkit",
            versionRef = "spigradle"
        ),
        PluginDependency(
            "${project.group}.spigradle.nukkit-base",
            property("spigradle.version")!!.toString(),
            "nukkitBase",
            versionRef = "spigradle"
        ),
    )
    val nukkitPluginLibs = nukkitPlugins.map {
        it.toLibrary()
    }
    libraries.set(
        NukkitDependencies.entries.map { it.dependency }
                + nukkitPluginLibs
    )
    plugins.set(nukkitPlugins)
}