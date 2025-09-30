package io.typst.spigradle.spigot

import io.typst.spigradle.spigot.SpigotPlugin
import io.typst.spigradle.util.testGradleTaskWithResource
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
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