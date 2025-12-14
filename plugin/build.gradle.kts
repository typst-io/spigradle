plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("spigradle-docs")
    id("spigradle-publish")
}

description = "An intelligent Gradle plugin for developing Minecraft resources."

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { setUrl("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.asm)
    implementation(libs.snakeyamlEngine)
    implementation(libs.gradlePlugin.ideaExt)
    implementation(libs.apache.commons.lang3)
    implementation(libs.apache.commons.text)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(gradleTestKit())
    testImplementation("io.typst:catalog")
}

configurations {
    testImplementation.get().dependencies += implementation.get().dependencies
}

tasks {
    test {
        useJUnitPlatform()
        maxParallelForks = 8
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
}