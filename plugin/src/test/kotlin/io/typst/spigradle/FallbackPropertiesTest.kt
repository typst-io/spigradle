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

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FallbackPropertiesTest {
    @TempDir
    lateinit var dir: File

    private fun yamlTask(properties: Map<String, Any>, fallbackFile: File): YamlGenerate {
        val project = ProjectBuilder.builder().withProjectDir(dir).build()
        return project.tasks.register("yaml", YamlGenerate::class.java).get().apply {
            this.properties.set(properties)
            mandatoryProperties.set(setOf("main"))
            fallbackFiles.set(mapOf("main" to project.layout.projectDirectory.file(fallbackFile.name)))
        }
    }

    @Test
    fun `explicit main class takes precedence over detected fallback`() {
        val fallbackFile = dir.resolve("main.txt").apply { writeText("example.DetectedMain") }
        val task = yamlTask(mapOf("main" to "example.ExplicitMain"), fallbackFile)

        assertEquals(mapOf("main" to "example.ExplicitMain"), task.readProperties())
    }

    @Test
    fun `reads fallback at execution time after detection writes it`() {
        val fallbackFile = dir.resolve("main.txt")
        val task = yamlTask(mapOf("name" to "Example"), fallbackFile)

        assertEquals(mapOf("name" to "Example"), task.readProperties())

        fallbackFile.writeText("example.DetectedMain")

        assertEquals(mapOf("name" to "Example", "main" to "example.DetectedMain"), task.readProperties())
    }

    @Test
    fun `fails before writing when a mandatory property is missing`() {
        val output = dir.resolve("plugin.yml")
        val task = yamlTask(mapOf("name" to "Example"), dir.resolve("missing.txt"))
        task.outputFiles.from(output)

        assertFailsWith<GradleException> { task.generate() }
        assertEquals(false, output.exists())
    }
}
