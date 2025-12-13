import io.typst.spigradle.common.Dependencies
import io.typst.spigradle.common.Dependency
import io.typst.spigradle.common.SpigotDependencies

plugins {
    java
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = "1.0.0"
description = "Spigot version catalog for Gradle"

spigradleCatalog {
    val spigotDeps = SpigotDependencies.entries.map { it.dependency }
    val commonDeps = Dependencies.entries.map { it.dependency }
    libraries.set(spigotDeps + commonDeps)
    plugins.set(
        listOf(
            Dependency(
                project.group.toString(),
                "spigradle",
                rootProject.version.toString(),
                "spigot",
                versionRef = "spigradle"
            ),
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
