import io.typst.spigradle.bungee.*

plugins {
    kotlin("jvm") version "2.2.0"
    alias(bungees.plugins.bungee)
}

repositories {
    mavenCentral()
    bungeeRepos {
        sonatype()
        minecraftLibraries()
    }
}

dependencies {
    compileOnly(bungees.bungeecord.api)
}

bungee {
    author = "EntryPoint"
}