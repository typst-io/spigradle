import io.typst.spigradle.catalog.CommonDependencies
import io.typst.spigradle.catalog.PluginDependency

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.common.version")!!
description = "Spigradle common version catalog for Gradle"

spigradleCatalog {
    val commonPlugins = listOf(
        PluginDependency(
            "org.jetbrains.gradle.plugin.idea-ext",
            "1.3",
            "ideaExt",
            versionRef = "ideaExt",
        )
    )
    val commonPluginLibs = commonPlugins.map {
        it.toLibrary()
    }
    libraries.set(CommonDependencies.entries.map { it.dependency } + commonPluginLibs)
    plugins.set(commonPlugins)
}
