pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "spigradle"

include("plugin", "spigot-catalog", "bungee-catalog", "nukkit-catalog", "common-catalog")
includeBuild("build-logic")
includeBuild("common")