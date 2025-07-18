@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
}

val generateSpigradleMeta by tasks.registering {
    group = "spigradle build"

    val version = project.version
    inputs.property("version", version)

    val generatedSourceDir = layout.buildDirectory.dir("generated/source/spigradle-build/kotlin/main")
    outputs.dir(generatedSourceDir)

    doLast {
        val outputDir = generatedSourceDir.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        outputDir.resolve("SpigradleMeta.kt").writeText(
            """
            package io.typst.spigradle

            object SpigradleMeta {
                const val VERSION = "$version"
            }
            """.trimIndent()
        )
    }
}

sourceSets.main {
    kotlin.srcDir(generateSpigradleMeta)
}
