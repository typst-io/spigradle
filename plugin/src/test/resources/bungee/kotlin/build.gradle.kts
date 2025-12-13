import io.typst.spigradle.bungee.*

plugins {
    kotlin("jvm") version "2.2.0"
    id("io.typst.spigradle.bungee")
}

repositories {
    mavenCentral()
    bungeeRepos {
        sonatype()
        minecraftLibraries()
    }
}

dependencies {
    compileOnly(bungees.bungeecord)
}

bungee {
    author = "EntryPoint"
}