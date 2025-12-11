rootProject.name = "spigradle-common"

pluginManagement {
    includeBuild("../build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
