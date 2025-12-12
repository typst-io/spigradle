import io.typst.spigradle.common.BungeeDependencies
import io.typst.spigradle.common.Dependencies
import io.typst.spigradle.common.Dependency
import org.apache.commons.text.CaseUtils

plugins {
    java
    id("io.typst.spigradle.catalog") // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.central.publish") // build-logic/central-publish
}

version = "1.0.0"
description = "Spigradle common version catalog for Gradle"

spigradleCatalog {
    val commonDeps = Dependencies.entries.map { it.dependency }
    libraries.set(commonDeps)
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

publishing {
    publications {
        val moduleName = CaseUtils.toCamelCase(project.name, false)
        named<MavenPublication>(moduleName) {
            from(components["versionCatalog"])
        }
    }
}
