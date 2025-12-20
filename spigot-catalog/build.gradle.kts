import io.typst.spigradle.catalog.PluginDependency
import io.typst.spigradle.catalog.PaperDependencies

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.spigot.version")!!
description = "Spigot version catalog for Gradle"

spigradleCatalog {
    val spigotPlugins = listOf(
        PluginDependency(
            "${project.group}.spigradle.spigot",
            property("spigradle.version")!!.toString(),
            "spigot",
            versionRef = "spigradle"
        ),
        PluginDependency(
            "${project.group}.spigradle.spigot-base",
            property("spigradle.version")!!.toString(),
            "spigotBase",
            versionRef = "spigradle"
        ),
    )
    val spigotPluginLibs = spigotPlugins.map {
        it.toLibrary()
    }
    libraries.set(
        PaperDependencies.entries.map { it.dependency }
                + spigotPluginLibs
    )
    plugins.set(spigotPlugins)
}
