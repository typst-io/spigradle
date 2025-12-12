import io.typst.spigradle.common.Dependencies
import io.typst.spigradle.common.Dependency
import io.typst.spigradle.common.NukkitDependencies
import org.apache.commons.text.CaseUtils

plugins {
    java
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = "1.0.0"
description = "NukkitX version catalog for Gradle"

spigradleCatalog {
    val nukkitDeps = NukkitDependencies.entries.map { it.dependency }
    val commonDeps = Dependencies.entries.map { it.dependency }
    libraries.set(nukkitDeps + commonDeps)
    plugins.set(
        listOf(
            Dependency(
                project.group.toString(),
                "spigradle.nukkit",
                rootProject.version.toString(),
                "nukkit",
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

publishing {
    publications {
        val moduleName = CaseUtils.toCamelCase(project.name, false)
        named<MavenPublication>(moduleName) {
            from(components["versionCatalog"])
        }
    }
}
