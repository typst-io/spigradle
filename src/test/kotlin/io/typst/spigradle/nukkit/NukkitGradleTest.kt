package io.typst.spigradle.nukkit

import io.typst.spigradle.nukkit.NukkitPlugin
import io.typst.spigradle.util.testGradleTaskWithResource
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class NukkitGradleTest {
    @Test
    fun kotlin() {
        testGradleTaskWithResource("/nukkit/kotlin", NukkitPlugin.genDescTask)
    }

    @Test
    fun groovy() {
        testGradleTaskWithResource("/nukkit/groovy", NukkitPlugin.genDescTask)
    }
}