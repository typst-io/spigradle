package io.typst.spigradle.spigot

import io.typst.spigradle.util.testGradleTaskWithResource
import kotlin.test.Test

class SpigotGradleTest {
    @Test
    fun kotlin() {
        testGradleTaskWithResource("/spigot/kotlin", SpigotPlugin.genDescTask)
    }

    @Test
    fun groovy() {
        testGradleTaskWithResource("/spigot/groovy", SpigotPlugin.genDescTask)
    }

    @Test
    fun shadowLibraries() {
        testGradleTaskWithResource("/spigot/shadowLibraries", SpigotPlugin.genDescTask)
    }
}