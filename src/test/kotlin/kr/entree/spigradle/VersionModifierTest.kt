package kr.entree.spigradle

import io.typst.spigradle.VersionModifier
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionModifierTest {
    @Test
    fun `snapshot appender`() {
        assertEquals("1.0-SNAPSHOT", VersionModifier.SNAPSHOT_APPENDER("1.0"))
        assertEquals("1.0-ABC", VersionModifier.SNAPSHOT_APPENDER("1.0-ABC"))
    }

    @Test
    fun `spigot adjuster`() {
        assertEquals("1.0-R0.1-SNAPSHOT", VersionModifier.SPIGOT_ADJUSTER("1.0"))
        assertEquals("1.0-R0.1-ABC", VersionModifier.SPIGOT_ADJUSTER("1.0-R0.1-ABC"))
        assertEquals("1.0-R2-SNAPSHOT", VersionModifier.SPIGOT_ADJUSTER("1.0-R2-SNAPSHOT"))
    }
}