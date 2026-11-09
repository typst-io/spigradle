/*
 * Copyright (c) 2026 Spigradle contributors.
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

import io.typst.spigradle.spigot.PaperDownloadTask.Companion.findLatestBuild
import io.typst.spigradle.spigot.PaperDownloadTask.Companion.isDownloaded
import io.typst.spigradle.sha256Of
import java.net.URI
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// https://github.com/typst-io/spigradle/issues/122
class PaperDownloadTaskTest {
    // the shape of https://fill.papermc.io/v3/projects/paper/versions/{version}/builds
    private fun build(id: Int, channel: String, sha256: String = "sha-$id") = """
        {
          "id": $id,
          "time": "2025-09-06T12:50:11Z",
          "channel": "$channel",
          "commits": [],
          "downloads": {
            "server:default": {
              "name": "paper-1.21.10-$id.jar",
              "checksums": { "sha256": "$sha256" },
              "size": 52811717,
              "url": "https://fill-data.papermc.io/v1/objects/$sha256/paper-1.21.10-$id.jar"
            }
          }
        }
    """.trimIndent()

    private fun builds(vararg builds: String) = builds.joinToString(",", "[", "]")

    @Test
    fun `selects the latest build regardless of channel`() {
        val json = builds(
            build(128, "STABLE"),
            build(131, "ALPHA"),
            build(129, "STABLE"),
            build(130, "BETA"),
        )

        assertEquals(
            PaperBuild(
                131,
                URI("https://fill-data.papermc.io/v1/objects/sha-131/paper-1.21.10-131.jar"),
                "sha-131"
            ),
            findLatestBuild(json, "1.21.10")
        )
    }

    @Test
    fun `fails when there is no build`() {
        val exception = assertFailsWith<IllegalStateException> { findLatestBuild(builds(), "1.22") }

        assertContains(exception.message.orEmpty(), "1.22")
    }

    @Test
    fun `fails when the build has no checksum`() {
        val json = builds(build(1, "STABLE").replace(""""checksums": { "sha256": "sha-1" },""", ""))

        assertFailsWith<IllegalStateException> { findLatestBuild(json, "1.21.10") }
    }

    @Test
    fun `fails on an unknown response`() {
        assertFailsWith<IllegalStateException> { findLatestBuild("""{"error": "version not found"}""", "0.0") }
    }

    @Test
    fun `downloads again only when the cached jar isn't the latest build`() {
        val jar = Files.createTempFile("paper", ".jar")
        try {
            Files.writeString(jar, "paper build 130")
            val cachedSha = sha256Of(jar)
            val url = URI("https://example.com/paper.jar")

            assertTrue(isDownloaded(jar, PaperBuild(130, url, cachedSha.uppercase())))
            assertFalse(isDownloaded(jar, PaperBuild(131, url, "sha-131")))
            Files.delete(jar)
            assertFalse(isDownloaded(jar, PaperBuild(130, url, cachedSha)))
        } finally {
            Files.deleteIfExists(jar)
        }
    }
}
