import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.util.*

plugins {
    `kotlin-dsl`
}

// NOTE: https://github.com/gradle/gradle/issues/21144
val rootProperties: Properties = Properties().apply {
    load(file("../gradle.properties").bufferedReader())
}
for ((k, v) in rootProperties.entries) {
    extra.set(k.toString(), v)
}

allprojects {
    group = "io.typst"

    pluginManager.withPlugin("java") {
        val java = extensions["java"] as JavaPluginExtension
        java.toolchain {
            languageVersion.set(JavaLanguageVersion.of(property("java.version")!!.toString().toInt()))
        }
    }

    pluginManager.withPlugin("kotlin") {
        val kotlin = extensions["kotlin"] as KotlinJvmProjectExtension
        kotlin.compilerOptions {
            apiVersion = KotlinVersion.fromVersion(property("kotlin.version")!!.toString())
            languageVersion = KotlinVersion.fromVersion(property("kotlin.version")!!.toString())
        }
    }
}