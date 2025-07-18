import io.typst.spigradle.build.VersionTask
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `spigradle-meta`
    `spigradle-publish`
    `spigradle-docs`
}

group = "kr.entree"
version = VersionTask.readVersion(project)
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
    implementation(libs.ideaExt)
    implementation(libs.downloadTask)
    implementation(libs.snakeyamlEngine)
    compileOnly(libs.spigotApi)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(gradleTestKit())
}

configurations {
    testImplementation.get().dependencies += implementation.get().dependencies
}

kotlin {
    compilerOptions {
        // Set lower API and language version to make the plugins compatible with Gradle 8.0+
        // See: https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion = KotlinVersion.KOTLIN_1_8
        languageVersion = KotlinVersion.KOTLIN_1_8
    }
}

tasks {
    test {
        useJUnitPlatform()
        maxParallelForks = 4
        testLogging {
            events("passed", "skipped", "failed")
        }
        dependsOn(getByName("publishToMavenLocal"))
    }
}