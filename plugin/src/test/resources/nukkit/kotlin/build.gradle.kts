import io.typst.spigradle.nukkit.*

plugins {
    kotlin("jvm") version "2.2.0"
    id("io.typst.spigradle.nukkit")
}

repositories {
    mavenCentral()
    nukkitRepos {
        openCollabRelease()
        openCollabSnapshot()
    }
}

dependencies {
    compileOnly(nukkits.nukkitX)
}

nukkit {
    authors = listOf("EntryPoint")
}