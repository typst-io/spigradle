import io.typst.spigradle.bungee.*

plugins {
    kotlin("jvm") version "2.2.0"
    id("io.typst.spigradle.bungee")
}

repositories {
    mavenCentral()
    sonatype()
    minecraftLibraries()
}

dependencies {
    compileOnly(bungeecord())
}

bungee {
    author = "EntryPoint"
}