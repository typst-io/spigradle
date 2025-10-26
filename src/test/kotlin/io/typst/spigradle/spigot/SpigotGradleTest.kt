package io.typst.spigradle.spigot

import io.typst.spigradle.util.testGradleTaskWithResource
import kotlin.test.Test

class SpigotGradleTest {
    @Test
    fun kotlin() {
        testGradleTaskWithResource("/spigot/kotlin", SpigotPlugin.SPIGOT_TYPE.descGenTask)
    }

    @Test
    fun groovy() {
        testGradleTaskWithResource("/spigot/groovy", SpigotPlugin.SPIGOT_TYPE.descGenTask)
    }

    @Test
    fun shadowLibraries() {
        testGradleTaskWithResource("/spigot/shadowLibraries", SpigotPlugin.SPIGOT_TYPE.descGenTask)
    }
}