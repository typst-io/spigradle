import io.typst.spigradle.common.Dependencies
import io.typst.spigradle.common.Dependency
import io.typst.spigradle.common.SpigotDependencies

plugins {
    // build-logic/**/SpigradleCatalogPlugin.kt
    id("io.typst.spigradle.catalog")
}

version = "1.0.0"

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
            )
        )
    )
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}
