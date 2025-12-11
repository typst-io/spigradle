import io.typst.spigradle.nukkit.*

plugins {
    kotlin("jvm") version "2.2.0"
    id("io.typst.spigradle.nukkit")
}

repositories {
    mavenCentral()
    openCollabRelease()
    openCollabSnapshot()
}

dependencies {
    compileOnly(nukkit())
}

nukkit {
    authors = listOf("EntryPoint")
}