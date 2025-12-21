import io.typst.spigradle.catalog.BungeeDependencies
import io.typst.spigradle.catalog.BungeeVersions
import io.typst.spigradle.catalog.PluginDependency
import io.typst.spigradle.catalog.Version

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.bungee.version")!!
description = "BungeeCord version catalog for Gradle"

spigradleCatalog {
    val spigradleVersion = Version(property("spigradle.version")!!.toString(), "spigradle")
    val bungeePlugins = listOf(
        PluginDependency(
            "${project.group}.spigradle.bungee",
            spigradleVersion,
            "bungee",
        ),
        PluginDependency(
            "${project.group}.spigradle.bungee-base",
            spigradleVersion,
            "bungeeBase",
        ),
    )
    val bungeePluginLibs = bungeePlugins.map {
        it.toLibrary()
    }
    versions.set(BungeeVersions.entries.map { it.version } + spigradleVersion)
    libraries.set(
        BungeeDependencies.entries.map { it.dependency }
                + bungeePluginLibs
    )
    plugins.set(bungeePlugins)
}