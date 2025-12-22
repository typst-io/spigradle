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
    val shadowJarVersion = Version("9.3.0", "shadowJar")
    val commonVersions = listOf(ideaExtVersion, shadowJarVersion)
    val commonPlugins = listOf(
        PluginDependency(
            "org.jetbrains.gradle.plugin.idea-ext",
            ideaExtVersion,
            "ideaExt",
        ),
        PluginDependency(
            "com.gradleup.shadow",
            shadowJarVersion,
            "shadowJar"
        ),
        PluginDependency(
            "org.jooq.jooq-codegen-gradle",
            CommonVersions.JOOQ.version,
            "jooq"
        )
    )
    val commonPluginLibs = commonPlugins.map {
        it.toLibrary()
    }
    versions.set(CommonVersions.entries.map { it.version } + commonVersions)
    libraries.set(CommonDependencies.entries.map { it.dependency } + commonPluginLibs)
    plugins.set(commonPlugins)
}