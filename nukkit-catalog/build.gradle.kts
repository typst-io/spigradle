import io.typst.spigradle.catalog.NukkitDependencies
import io.typst.spigradle.catalog.NukkitVersions
import io.typst.spigradle.catalog.PluginDependency
import io.typst.spigradle.catalog.Version

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.nukkit.version")!!
description = "NukkitX version catalog for Gradle"

spigradleCatalog {
    val spigradleVersion = Version(property("spigradle.version")!!.toString(), "spigradle")
    val nukkitPlugins = listOf(
        PluginDependency(
            "${project.group}.spigradle.nukkit",
            spigradleVersion,
            "nukkit",
        ),
        PluginDependency(
            "${project.group}.spigradle.nukkit-base",
            spigradleVersion,
            "nukkitBase",
        ),
    )
    val nukkitPluginLibs = nukkitPlugins.map {
        it.toLibrary()
    }
    versions.set(NukkitVersions.entries.map { it.version } + spigradleVersion)
    libraries.set(
        NukkitDependencies.entries.map { it.dependency }
                + nukkitPluginLibs
    )
    plugins.set(nukkitPlugins)
}