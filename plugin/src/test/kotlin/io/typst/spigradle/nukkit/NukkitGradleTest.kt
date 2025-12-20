package io.typst.spigradle.nukkit

import io.typst.spigradle.util.testGradleTaskWithResource
import org.junit.jupiter.api.Test

class NukkitGradleTest {
    @Test
    fun kotlin() {
        testGradleTaskWithResource("/nukkit/kotlin", NukkitPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME)
    }

    @Test
    fun groovy() {
        testGradleTaskWithResource("/nukkit/groovy", NukkitPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME)
    }
}