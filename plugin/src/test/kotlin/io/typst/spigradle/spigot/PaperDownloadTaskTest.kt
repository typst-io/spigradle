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
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `selects the latest stable build skipping newer experimental builds`() {
        val json = builds(
            build(131, "ALPHA"),
            build(130, "BETA"),
            build(128, "STABLE"),
            build(129, "STABLE"),
        )

        assertEquals(
            PaperBuild(
                129,
                URI("https://fill-data.papermc.io/v1/objects/sha-129/paper-1.21.10-129.jar"),
                "sha-129"
            ),
            findLatestBuild(json, "1.21.10")
        )
    }

    @Test
    fun `fails with the available channels when there is no stable build`() {
        val json = builds(build(2, "BETA"), build(1, "ALPHA"))

        val exception = assertFailsWith<IllegalStateException> { findLatestBuild(json, "1.22") }

        assertContains(exception.message.orEmpty(), "1.22")
        assertContains(exception.message.orEmpty(), "[ALPHA, BETA]")
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
}
