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
    implementation(libs.kotlinx.collections.immutable)
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
        // the test projects resolve the catalogs from this repository, published by `SpigradleCatalogPublishPlugin`
        val testRepo = isolated.rootProject.projectDirectory.dir("build/spigradle-test-repo")
        dependsOn(listOf("spigot", "bungee", "nukkit", "common").map {
            ":$it-catalog:publishAllPublicationsToSpigradleTestRepository"
        })
        systemProperty("spigradle.test.repo", testRepo.asFile.toURI().toString())
    }

    val writeVersionFile by registering {
        val versionText = version.toString()
        val output = isolated.rootProject.projectDirectory.file("version.txt")
        inputs.property("version", versionText)
        outputs.file(output)
        doLast {
            output.asFile.writeText(versionText)
        }
    }

    assemble {
        dependsOn(writeVersionFile)
    }

    dokka {
        moduleName = "spigradle"
    }
}
