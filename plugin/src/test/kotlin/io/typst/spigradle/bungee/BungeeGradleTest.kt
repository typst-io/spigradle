package io.typst.spigradle.bungee

import io.typst.spigradle.util.testGradleTaskWithResource
import org.junit.jupiter.api.Test

class BungeeGradleTest {
    @Test
    fun kotlin() {
        testGradleTaskWithResource("/bungee/kotlin", BungeePlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME)
    }

    @Test
    fun groovy() {
        // NOTE: This will crash if versioning up Kotlin! maybe conflicts Kotlin 1.5+ on JVM 8.
        testGradleTaskWithResource("/bungee/groovy", BungeePlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME)
    }
}