plugins {
    `kotlin-dsl`
    id("spigradle-versioning")
}

repositories {
    mavenCentral()
}

subprojects {
    group = "io.typst"
}