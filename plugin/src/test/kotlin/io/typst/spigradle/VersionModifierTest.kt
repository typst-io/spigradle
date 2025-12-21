package io.typst.spigradle

import io.typst.spigradle.catalog.Dependency
import io.typst.spigradle.catalog.Version
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionModifierTest {
    val initial = Dependency("", "", Version(""), "")

    @Test
    fun `snapshot appender`() {
        val dep = initial.copy(tags = Dependency.SNAPSHOT_TAG)
        assertEquals("1.0-SNAPSHOT", dep.getTaggedVersion("1.0"))
        assertEquals("1.0-ABC", dep.getTaggedVersion("1.0-ABC"))
    }

    @Test
    fun `spigot adjuster`() {
        val dep = initial.copy(tags = Dependency.SPIGOT_VERSION_TAGS)
        assertEquals("1.0-R0.1-SNAPSHOT", dep.getTaggedVersion("1.0"))
        assertEquals("1.0-R0.1-ABC", dep.getTaggedVersion("1.0-R0.1-ABC"))
        assertEquals("1.0-R2-SNAPSHOT", dep.getTaggedVersion("1.0-R2-SNAPSHOT"))
    }
}