package io.typst.spigradle.spigot

import io.typst.spigradle.util.testGradleTaskWithResource
import kotlin.test.Test

class SpigotGradleTest {
    @Test
    fun kotlin() {
        testGradleTaskWithResource("/spigot/kotlin", SpigotPlugin.GENERATE_DESCRIPTION_TASK_NAME)
    }

    @Test
    fun groovy() {
        testGradleTaskWithResource("/spigot/groovy", SpigotPlugin.GENERATE_DESCRIPTION_TASK_NAME)
    }

    @Test
    fun shadowLibraries() {
        testGradleTaskWithResource("/spigot/shadowLibraries", SpigotPlugin.GENERATE_DESCRIPTION_TASK_NAME)
    }
}