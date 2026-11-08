plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        // the catalogs published by the plugin build, see `spigradleTest` repository in build-logic
        maven(providers.gradleProperty("spigradle.test.repo").get())
        mavenCentral()
    }
    versionCatalogs {
        create("papers") {
            from("io.typst:paper-catalog:1.0.0")
        }
    }
}

rootProject.name = "SpigradlePaperTestKotlin"
