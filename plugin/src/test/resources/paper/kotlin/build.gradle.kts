plugins {
    kotlin("jvm") version "2.2.0"
    alias(papers.plugins.paper)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(papers.paper.api)
}

paper {
    apiVersion = "1.21"
    description = "A sample Paper plugin"
}

tasks {
    build.get().doLast {
        val pluginFile = File(sourceSets["main"].output.resourcesDir, "paper-plugin.yml")
        if (pluginFile.isFile) {
            println(pluginFile.absolutePath)
            println(pluginFile.bufferedReader().readText())
        } else {
            throw GradleException("Error!")
        }
    }
}