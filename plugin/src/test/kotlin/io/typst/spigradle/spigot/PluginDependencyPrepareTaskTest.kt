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

import com.sun.net.httpserver.HttpServer
import io.typst.spigradle.ContentSource
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testfixtures.internal.ProjectBuilderImpl
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.test.*

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

    private fun jarBytes(pluginName: String): ByteArray = ByteArrayOutputStream().also { out ->
        JarOutputStream(out).use { jar ->
            jar.putNextEntry(JarEntry("plugin.yml"))
            jar.write("name: $pluginName\n".toByteArray())
            jar.closeEntry()
        }
    }.toByteArray()

    /**
     * Runs [test] against a fake Spiget API, which answers every search with [searchResultIds] and serves a jar
     * named by [pluginNameById], or 404 if absent. The ids downloaded are collected in order.
     */
    private fun withSpiget(
        searchResultIds: List<Int>,
        pluginNameById: Map<Int, String>,
        test: (spigetApi: URI, downloadedIds: List<Int>) -> Unit,
    ) {
        val downloadedIds = CopyOnWriteArrayList<Int>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/v2/search/resources/") { exchange ->
            exchange.use {
                val body = searchResultIds.joinToString(",", "[", "]") { id -> """{"id": $id}""" }.toByteArray()
                it.sendResponseHeaders(200, body.size.toLong())
                it.responseBody.write(body)
            }
        }
        // /v2/resources/{id}/download
        server.createContext("/v2/resources/") { exchange ->
            exchange.use {
                val id = it.requestURI.path.split("/")[3].toInt()
                downloadedIds += id
                val pluginName = pluginNameById[id]
                if (pluginName == null) {
                    it.sendResponseHeaders(404, -1)
                } else {
                    val body = jarBytes(pluginName)
                    it.sendResponseHeaders(200, body.size.toLong())
                    it.responseBody.write(body)
                }
            }
        }
        server.start()
        try {
            test(URI("http://127.0.0.1:${server.address.port}/v2/"), downloadedIds)
        } finally {
            server.stop(0)
        }
    }

    private fun SpigotPluginDependency.downloadUri(): URI = (contentSource as ContentSource.Memory).uri

    // https://github.com/typst-io/spigradle/issues/124
    @Test
    fun `stops downloading at the first candidate with the matching plugin name`() = withSpiget(
        searchResultIds = listOf(1, 2, 3),
        pluginNameById = mapOf(1 to "VaultChat", 2 to "Vault", 3 to "Vault"),
    ) { spigetApi, downloadedIds ->
        val dependency = prepareTask().downloadPluginDependency("Vault", spigetApi)

        assertEquals("Vault", dependency?.name)
        // the resolved resource id is kept to report where the jar came from
        assertEquals(spigetApi.resolve("resources/2/download"), dependency?.downloadUri())
        assertEquals(listOf(1, 2), downloadedIds)
    }

    // https://github.com/typst-io/spigradle/issues/124
    @Test
    fun `skips candidates failing to download`() = withSpiget(
        searchResultIds = listOf(1, 2),
        pluginNameById = mapOf(2 to "Vault"),
    ) { spigetApi, _ ->
        val dependency = prepareTask().downloadPluginDependency("Vault", spigetApi)

        assertEquals(spigetApi.resolve("resources/2/download"), dependency?.downloadUri())
    }

    // https://github.com/typst-io/spigradle/issues/124
    @Test
    fun `downloads a limited number of candidates when nothing matches`() = withSpiget(
        searchResultIds = (1..10).toList(),
        pluginNameById = (1..10).associateWith { "Other$it" },
    ) { spigetApi, downloadedIds ->
        val dependency = prepareTask().downloadPluginDependency("Vault", spigetApi)

        assertNull(dependency)
        assertEquals((1..PluginDependencyPrepareTask.MAX_SPIGET_CANDIDATES).toList(), downloadedIds)
    }

    @Test
    fun `downloads the only search result`() = withSpiget(
        searchResultIds = listOf(1),
        pluginNameById = mapOf(1 to "Vault"),
    ) { spigetApi, _ ->
        val dependency = prepareTask().downloadPluginDependency("Vault", spigetApi)

        assertEquals(spigetApi.resolve("resources/1/download"), dependency?.downloadUri())
    }

    @Test
    fun `finds the plugin at the last candidate`() = withSpiget(
        searchResultIds = listOf(1, 2, 3, 4),
        pluginNameById = mapOf(1 to "VaultChat", 2 to "VaultUnlocked", 3 to "Vault", 4 to "Vault"),
    ) { spigetApi, downloadedIds ->
        val dependency = prepareTask().downloadPluginDependency("Vault", spigetApi)

        assertEquals(spigetApi.resolve("resources/3/download"), dependency?.downloadUri())
        assertEquals(listOf(1, 2, 3), downloadedIds)
    }

    @Test
    fun `returns nothing when there is no search result`() = withSpiget(
        searchResultIds = emptyList(),
        pluginNameById = emptyMap(),
    ) { spigetApi, downloadedIds ->
        assertNull(prepareTask().downloadPluginDependency("Vault", spigetApi))
        assertEquals(emptyList(), downloadedIds)
    }
}
