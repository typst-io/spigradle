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

import com.sun.net.httpserver.HttpServer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.InetSocketAddress
import java.net.URI
import kotlin.test.*

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

    private fun withServer(test: (HttpServer, URI) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.start()
        try {
            test(server, URI("http://127.0.0.1:${server.address.port}/server.jar"))
        } finally {
            server.stop(0)
        }
    }

    private fun assertExistingFilePreserved() {
        assertEquals("cached server", dir.resolve("server.jar").readText())
        assertEquals(emptyList(), dir.list()!!.filter { it.endsWith(".part") }, "temporary files are left")
    }

    // https://github.com/typst-io/spigradle/issues/121
    @Test
    fun `keeps existing file when the server responds an error`() = withServer { server, uri ->
        server.createContext("/server.jar") { exchange ->
            exchange.use {
                val body = "error page".toByteArray()
                it.sendResponseHeaders(500, body.size.toLong())
                it.responseBody.write(body)
            }
        }
        val task = downloadTask(overwrite = true, existingFile = true).apply { this.uri.set(uri) }

        assertFails { task.download() }

        assertExistingFilePreserved()
    }

    // https://github.com/typst-io/spigradle/issues/121
    @Test
    fun `keeps existing file when the connection is lost while downloading`() = withServer { server, uri ->
        server.createContext("/server.jar") { exchange ->
            // promise 1000 bytes but send only a part of them, then drop the connection
            exchange.sendResponseHeaders(200, 1000)
            exchange.responseBody.write(ByteArray(100))
            exchange.responseBody.flush()
            runCatching { exchange.close() }
        }
        val task = downloadTask(overwrite = true, existingFile = true).apply { this.uri.set(uri) }

        assertFails { task.download() }

        assertExistingFilePreserved()
    }
}
