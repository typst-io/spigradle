import io.typst.spigradle.catalog.CommonDependencies
import io.typst.spigradle.catalog.Dependency

plugins {
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = property("catalog.common.version")!!
description = "Spigradle common version catalog for Gradle"

spigradleCatalog {
    libraries.set(CommonDependencies.entries.map { it.dependency })
    plugins.set(
        listOf(
            Dependency(
                "org.jetbrains",
                "gradle.plugin.idea-ext",
                "1.3",
                "ideaExt",
                versionRef = "ideaExt",
            )
        )
    )
}
