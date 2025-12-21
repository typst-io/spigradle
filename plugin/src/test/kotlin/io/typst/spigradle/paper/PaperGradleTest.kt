package io.typst.spigradle.paper

import io.typst.spigradle.util.testGradleTaskWithResource
import kotlin.test.Ignore
import kotlin.test.Test

class PaperGradleTest {
    @Test
    @Ignore
    fun kotlin() {
        testGradleTaskWithResource("/paper/kotlin", PaperPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME)
    }

    @Test
    @Ignore
    fun groovy() {
        testGradleTaskWithResource("/paper/groovy", PaperPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME)
    }
}