import io.typst.spigradle.catalog.BungeeDependencies
import io.typst.spigradle.catalog.PluginDependency

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.bungee.version")!!
description = "BungeeCord version catalog for Gradle"

spigradleCatalog {
    val bungeePlugins = listOf(
        PluginDependency(
            "${project.group}.spigradle.bungee",
            property("spigradle.version")!!.toString(),
            "bungee",
            versionRef = "spigradle"
        ),
        PluginDependency(
            "${project.group}.spigradle.bungee-base",
            property("spigradle.version")!!.toString(),
            "bungeeBase",
            versionRef = "spigradle"
        ),
    )
    val bungeePluginLibs = bungeePlugins.map {
        it.toLibrary()
    }
    libraries.set(
        BungeeDependencies.entries.map { it.dependency }
                + bungeePluginLibs
    )
    plugins.set(
        bungeePlugins
    )
}