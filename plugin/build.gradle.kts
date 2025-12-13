plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("spigradle-docs")
    id("spigradle-publish")
    id("spigradle-versioning")
    id("spigradle-java")
}

description = "An intelligent Gradle plugin for developing Minecraft resources."

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { setUrl("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    api("io.typst:spigradle-common:${project.version}") // included build `common`
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
}

configurations {
    testImplementation.get().dependencies += implementation.get().dependencies
}

kotlin {
    compilerOptions {
        // Set lower API and language version to make the plugins compatible with Gradle 8.0+
        // See: https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2
    }
}

tasks {
    test {
        useJUnitPlatform()
        maxParallelForks = 4
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}