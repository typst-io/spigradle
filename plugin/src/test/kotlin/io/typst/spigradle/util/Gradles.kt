package io.typst.spigradle.util

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun Any.testGradleTaskWithResource(path: String, resultTaskName: String, taskName: String = "build") {
    val projectDir = File(javaClass.getResource(path)!!.file)
    val testKitHome = projectDir.resolve("test-kit-home").apply { mkdirs() }
    val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withTestKitDir(testKitHome)
        .withPluginClasspath()
        .withArguments(taskName, "--stacktrace")
        .withGradleVersion("9.0.0")
        .build()
    println(result.output)
    assertNotEquals(TaskOutcome.FAILED, result.task(":$resultTaskName")!!.outcome)
}

fun testGradleScript(
    dir: File,
    buildScript: String,
    extension: String = "gradle",
    settingsScript: String = """
            rootProject.name = "testProject"
        """.trimIndent(),
): GradleRunner = GradleRunner.create()
    .withProjectDir(dir.apply {
        dir.resolve("build.$extension").writeText(buildScript)
        dir.resolve("settings.$extension").writeText(settingsScript)
    })
    .withPluginClasspath()
    .withArguments("build", "--stacktrace")
    .withGradleVersion("8.14.3")


fun testGradleTask(
    taskName: String, dir: File,
    buildscript: String = """
        plugins {
            id 'io.typst.spigradle'
        }
    """.trimIndent(),
): BuildResult {
    File(dir, "build.gradle").writeText(buildscript)
    val result = GradleRunner.create()
        .withProjectDir(dir)
        .withArguments(taskName, "-s")
        .withPluginClasspath()
        .build()
    println("#### GradleRunner start")
    println(result.output)
    println("#### GradleRunner end")
    assertEquals(TaskOutcome.SUCCESS, result.task(":${taskName}")?.outcome)
    return result
}
