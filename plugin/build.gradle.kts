plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("spigradle-docs")
    id("spigradle-publish")
}

description = "An intelligent Gradle plugin for developing Minecraft resources."

version = property("spigradle.version")!!

dependencies {
    compileOnly(libs.spigot.api) // make dokka resolve the spigot-api class
    implementation(libs.kotlin.stdlib)
    implementation(libs.asm)
    implementation(libs.snakeyamlEngine)
    implementation(libs.ideaExt.plugin)
    implementation(libs.commons.lang3)
    implementation(libs.commons.text)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(gradleTestKit())
    testImplementation("io.typst:catalog")
}

configurations {
    testImplementation {
        extendsFrom(implementation.get())
    }
}

tasks {
    test {
        useJUnitPlatform()
        // Each fork starts its own TestKit daemons; more forks than ~1/3 of the cores slows down by contention
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 3).coerceAtLeast(1)
        testLogging {
            events("passed", "skipped", "failed")
        }
        // publish catalogs for test
        val catalogPublishTasks = rootProject.subprojects.filter {
            it.name.endsWith("-catalog")
        }.map {
            it.tasks.publishToMavenLocal
        }
        dependsOn(catalogPublishTasks)
    }

    assemble {
        doLast {
            project.rootDir.resolve("version.txt").writeText(version.toString())
        }
    }

    dokka {
        moduleName = "spigradle"
    }
}
