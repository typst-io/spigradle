plugins {
    `kotlin-dsl`
    `version-catalog`
    `maven-publish`
    signing
    id("spigradle-versioning")
    id("io.typst.spigradle.central.publish") // sourced in build-logic/central-publish
}

group = "io.typst"

repositories {
    mavenCentral()
}