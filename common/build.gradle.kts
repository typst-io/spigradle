plugins {
    `kotlin-dsl`
    `version-catalog`
    id("spigradle-versioning")
    id("spigradle-java")
    id("io.typst.spigradle.central.publish") // sourced in build-logic/central-publish
}

group = "io.typst"

repositories {
    mavenCentral()
}
