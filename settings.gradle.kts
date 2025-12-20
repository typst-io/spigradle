pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "spigradle"

include("plugin")
include("spigot-catalog", "bungee-catalog", "nukkit-catalog", "common-catalog")
include("spigot-bom-1.16", "spigot-bom-1.20")
includeBuild("build-logic")