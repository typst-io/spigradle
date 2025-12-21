import io.typst.spigradle.catalog.CommonDependencies
import io.typst.spigradle.catalog.CommonVersions
import io.typst.spigradle.catalog.PluginDependency
import io.typst.spigradle.catalog.Version

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.common.version")!!
description = "Spigradle common version catalog for Gradle"

spigradleCatalog {
    val ideaExtVersion = Version("1.3", "ideaExt")
    val commonPlugins = listOf(
        PluginDependency(
            "org.jetbrains.gradle.plugin.idea-ext",
            ideaExtVersion,
            "ideaExt",
        )
    )
    val commonPluginLibs = commonPlugins.map {
        it.toLibrary()
    }
    versions.set(CommonVersions.entries.map { it.version } + ideaExtVersion)
    libraries.set(CommonDependencies.entries.map { it.dependency } + commonPluginLibs)
    plugins.set(commonPlugins)
}