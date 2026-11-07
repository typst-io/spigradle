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

package io.typst.spigradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DownloadTest {
    @TempDir
    lateinit var dir: File

    private fun downloadTask(overwrite: Boolean, existingFile: Boolean): Download {
        val project = ProjectBuilder.builder().withProjectDir(dir).build()
        return project.tasks.register("download", Download::class.java).get().apply {
            this.overwrite.set(overwrite)
            outputFile.set(dir.resolve("server.jar").apply {
                if (existingFile) writeText("cached server")
            })
        }
    }

    @Test
    fun `downloads missing file by default`() {
        val task = downloadTask(overwrite = false, existingFile = false)

        assertTrue(task.onlyIf.isSatisfiedBy(task))
    }

    @Test
    fun `preserves existing file by default`() {
        val task = downloadTask(overwrite = false, existingFile = true)

        assertFalse(task.onlyIf.isSatisfiedBy(task))
    }

    @Test
    fun `downloads missing file when overwrite is enabled`() {
        val task = downloadTask(overwrite = true, existingFile = false)

        assertTrue(task.onlyIf.isSatisfiedBy(task))
    }

    @Test
    fun `replaces existing file when overwrite is enabled`() {
        val task = downloadTask(overwrite = true, existingFile = true)

        assertTrue(task.onlyIf.isSatisfiedBy(task))
    }
}
