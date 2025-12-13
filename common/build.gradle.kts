plugins {
    `kotlin-dsl`
    `version-catalog`
    `maven-publish`
    signing
    id("spigradle-versioning")
    id("spigradle-java")
    id("io.typst.spigradle.central.publish") // sourced in build-logic/central-publish
}

group = "io.typst"

repositories {
    mavenCentral()
}
