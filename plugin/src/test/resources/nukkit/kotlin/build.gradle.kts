import io.typst.spigradle.nukkit.*

plugins {
    kotlin("jvm") version "2.2.0"
    alias(nukkits.plugins.nukkit)
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