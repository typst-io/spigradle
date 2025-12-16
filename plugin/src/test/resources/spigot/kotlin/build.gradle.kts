import io.typst.spigradle.spigot.*

plugins {
    kotlin("jvm") version "2.2.0"
    id("io.typst.spigradle")
}

repositories {
    mavenCentral()
    spigotRepos {
        spigotmc()
    }
}

dependencies {
    compileOnly(spigots.spigot.api)
}

tasks {
    build.get().doLast {
        val pluginFile = File(sourceSets["main"].output.resourcesDir, "plugin.yml")
        if (pluginFile.isFile) {
            println(pluginFile.absolutePath)
            println(pluginFile.bufferedReader().readText())
        } else {
            throw GradleException("Error!")
        }
    }
}