dependencyResolutionManagement {
    repositories {
        // the catalogs published by the plugin build, see `spigradleTest` repository in build-logic
        maven(providers.gradleProperty("spigradle.test.repo").get())
        mavenCentral()
    }
    versionCatalogs {
        create("nukkits") {
            from("io.typst:nukkit-catalog:1.0.0")
        }
    }
}

rootProject.name = "SpigradleNukkitTest"
