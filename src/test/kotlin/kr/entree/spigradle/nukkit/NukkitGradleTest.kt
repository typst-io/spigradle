package kr.entree.spigradle.nukkit

import io.typst.spigradle.nukkit.NukkitPlugin
import kr.entree.spigradle.util.testGradleTaskWithResource
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

@Ignore
class NukkitGradleTest {
    @Test
    fun kotlin() {
        testGradleTaskWithResource("/nukkit/kotlin", NukkitPlugin.NUKKIT_TYPE.descGenTask)
    }

    @Test
    fun groovy() {
        testGradleTaskWithResource("/nukkit/groovy", NukkitPlugin.NUKKIT_TYPE.descGenTask)
    }
}