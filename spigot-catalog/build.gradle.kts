import io.typst.spigradle.catalog.PaperDependencies
import io.typst.spigradle.catalog.PaperVersions
import io.typst.spigradle.catalog.PluginDependency
import io.typst.spigradle.catalog.Version

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.spigot.version")!!
description = "Spigot version catalog for Gradle"

spigradleCatalog {
    val spigradleVersion = Version(property("spigradle.version")!!.toString(), "spigradle")
    val paperweightVersion = Version("2.0.0-beta.19", "paperweight")
    val spigotPlugins = listOf(
        PluginDependency(
            "${project.group}.spigradle.spigot",
            spigradleVersion,
            "spigot",
        ),
        PluginDependency(
            "${project.group}.spigradle.spigot-base",
            spigradleVersion,
            "spigotBase",
        ),
        PluginDependency(
            "io.papermc.paperweight.userdev",
            paperweightVersion,
            "paperweight-userdev"
        )
    )
    val spigotPluginLibs = spigotPlugins.map {
        it.toLibrary()
    }
    versions.set(PaperVersions.entries.map { it.version } + spigradleVersion + paperweightVersion)
    libraries.set(
        PaperDependencies.entries.map { it.dependency }
                + spigotPluginLibs
    )
    plugins.set(spigotPlugins)
}