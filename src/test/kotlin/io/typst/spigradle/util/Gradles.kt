package io.typst.spigradle.util

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun Any.testGradleTaskWithResource(path: String, taskName: String) {
    val result = GradleRunner.create()
        .withProjectDir(File(javaClass.getResource(path)!!.file))
        .withPluginClasspath()
        .withArguments("build", "--stacktrace")
        .withGradleVersion("5.4.1") // This is the minimal version required to use Spigradle.
        .build()
    println(result.output)
    assertNotEquals(TaskOutcome.FAILED, result.task(":$taskName")!!.outcome)
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
    .withGradleVersion("5.4.1")


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
