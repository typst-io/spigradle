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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testfixtures.internal.ProjectBuilderImpl
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PluginDependencyPrepareTaskTest {
    @TempDir
    lateinit var dir: File

    private lateinit var project: Project

    @AfterTest
    fun closeProject() {
        // Release dependency cache file handles before JUnit removes the directory on Windows.
        if (::project.isInitialized) ProjectBuilderImpl.stop(project)
    }

    private fun pluginJar(name: String, properties: String = ""): File = dir.resolve("$name.jar").apply {
        JarOutputStream(outputStream()).use { jar ->
            jar.putNextEntry(JarEntry("plugin.yml"))
            jar.write("name: $name\n$properties".toByteArray())
            jar.closeEntry()
        }
    }

    private fun prepareTask(vararg jars: File): PluginDependencyPrepareTask {
        project = ProjectBuilder.builder().withProjectDir(dir).build()
        val configuration = project.configurations.create("localPlugins")
        project.dependencies.add(configuration.name, project.files(*jars))
        val artifacts = configuration.incoming.artifactView { isLenient = true }.artifacts
        return project.tasks.register("prepare", PluginDependencyPrepareTask::class.java).get().apply {
            pluginNames.set(setOf("First"))
            localPluginFiles.from(PluginDependencyPrepareTask.localJarFiles(artifacts.resolvedArtifacts))
            outputDir.set(dir.resolve("plugins").apply { mkdirs() })
        }
    }

    @Test
    fun `copies recursive local dependencies and terminates on cycles`() {
        val first = pluginJar("First", "depend: [Second]\n")
        val second = pluginJar("Second", "depend: [First]\n")
        val task = prepareTask(first, second)

        task.prepare()

        assertContentEquals(first.readBytes(), dir.resolve("plugins/First.jar").readBytes())
        assertContentEquals(second.readBytes(), dir.resolve("plugins/Second.jar").readBytes())
        assertEquals(setOf("First.jar", "Second.jar"), dir.resolve("plugins").list()!!.toSet())
    }

    @Test
    fun `copies soft dependencies only when enabled and preserves existing jars`() {
        val first = pluginJar("First", "softdepend: [Optional]\n")
        val optional = pluginJar("Optional")
        val task = prepareTask(first, optional)

        task.prepare()

        assertFalse(dir.resolve("plugins/Optional.jar").exists())
        val existing = dir.resolve("plugins/First.jar")
        val cachedBytes = existing.readBytes()
        pluginJar("First", "softdepend: [Optional]\ndescription: updated classpath jar\n")
        task.downloadSoftDepend.set(true)

        task.prepare()

        assertContentEquals(cachedBytes, existing.readBytes())
        assertContentEquals(optional.readBytes(), dir.resolve("plugins/Optional.jar").readBytes())
    }

    // https://github.com/typst-io/spigradle/issues/116
    @Test
    fun `encodes plugin name with spaces as a spiget search path segment`() {
        val uri = PluginDependencyPrepareTask.spigetSearchUri("Holographic Displays")

        // Spiget treats '+' in the path literally (404), so a space must be '%20'
        assertEquals("/v2/search/resources/Holographic%20Displays", uri.rawPath)
        assertEquals("/v2/search/resources/Holographic Displays", uri.path)
        assertEquals("field=name&sort=-downloads", uri.rawQuery)
    }

    // https://github.com/typst-io/spigradle/issues/116
    @Test
    fun `encodes reserved characters in plugin name`() {
        val uri = PluginDependencyPrepareTask.spigetSearchUri("A/B?C#D")

        assertEquals("/v2/search/resources/A%2FB%3FC%23D", uri.rawPath)
        assertEquals("field=name&sort=-downloads", uri.rawQuery)
    }
}
