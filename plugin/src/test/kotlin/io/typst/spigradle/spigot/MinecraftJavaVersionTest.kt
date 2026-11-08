/*
 * Copyright (c) 2025 Spigradle contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.typst.spigradle.spigot

import io.typst.spigradle.spigot.SpigotPlugin.Companion.getMinecraftMinimumJavaVersion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// https://github.com/typst-io/spigradle/issues/114
class MinecraftJavaVersionTest {
    @Test
    fun legacyVersions() {
        assertEquals(8, getMinecraftMinimumJavaVersion("1.16.5"))
        assertEquals(16, getMinecraftMinimumJavaVersion("1.17.1"))
        assertEquals(17, getMinecraftMinimumJavaVersion("1.18.2"))
        assertEquals(17, getMinecraftMinimumJavaVersion("1.20.4"))
        assertEquals(21, getMinecraftMinimumJavaVersion("1.20.6"))
        assertEquals(21, getMinecraftMinimumJavaVersion("1.21"))
        assertEquals(21, getMinecraftMinimumJavaVersion("1.21.10"))
    }

    @Test
    fun yearBasedVersions() {
        for (version in listOf("26.1", "26.1.1", "26.2")) {
            val javaVersion = getMinecraftMinimumJavaVersion(version)
            assertTrue(javaVersion >= 25, "$version requires at least Java 21, but got $javaVersion")
        }
    }

    @Test
    fun preReleaseVersions() {
        assertEquals(21, getMinecraftMinimumJavaVersion("1.21.4-pre1"))
        assertEquals(21, getMinecraftMinimumJavaVersion("1.21.5-rc1"))
    }
}
