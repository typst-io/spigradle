/*
 * Copyright (c) 2021 Spigradle contributors.
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

package kr.entree.spigradle

import com.fasterxml.jackson.databind.ObjectMapper
import kr.entree.spigradle.spigot.SpigotDebugTask.downloadPaperTaskname
import kr.entree.spigradle.spigot.SpigotDebugTask.getPaperBuildsTaskname
import kr.entree.spigradle.spigot.SpigotDebugTask.getPaperDownloadsTaskname
import kr.entree.spigradle.spigot.SpigotDebugTask.paperBuildsJsonFilename
import kr.entree.spigradle.util.testGradleTask
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals

class DownloadTaskTest {
    @Test
    fun `get latest paper build`(@TempDir dir: File) {
        testGradleTask(getPaperBuildsTaskname, dir)
    }

    @Test
    fun `get latest paper downloads`(@TempDir dir: File) {
        testGradleTask(getPaperDownloadsTaskname, dir)
    }

    @Test
    fun `download latest paper`(@TempDir dir: File) {
        testGradleTask(downloadPaperTaskname, dir)
    }

    @Test
    fun `get depended version of paper`(@TempDir dir: File) {
        val version = "1.16.5"
        testGradleTask(getPaperBuildsTaskname, dir, """
            plugins {
                id 'kr.entree.spigradle'
            }
            
            dependencies {
                compileOnly(spigot('${version}'))
            }
        """.trimIndent())
        val jsonFile = dir.resolve("build").resolve("tmp").resolve(getPaperBuildsTaskname)
            .resolve(paperBuildsJsonFilename)
        val json = ObjectMapper().readTree(jsonFile)
        assertEquals(version, json.get("version").textValue())
    }
}
