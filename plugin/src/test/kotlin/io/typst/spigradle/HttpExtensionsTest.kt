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
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.*

class HttpExtensionsTest {
    @TempDir
    lateinit var dir: File

    private fun withServer(test: (HttpServer, URI) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.start()
        try {
            test(server, URI("http://127.0.0.1:${server.address.port}"))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `sends the user agent and preserves binary response bytes`() = withServer { server, baseUri ->
        val body = byteArrayOf(0, 1, 127, -128, -1)
        val userAgent = AtomicReference<String>()
        server.createContext("/binary") { exchange ->
            exchange.use {
                userAgent.set(it.requestHeaders.getFirst("User-Agent"))
                it.sendResponseHeaders(200, body.size.toLong())
                it.responseBody.write(body)
            }
        }

        assertContentEquals(body, fetchHttpGetAsByteArray(baseUri.resolve("/binary")).body())
        assertEquals("spigradle", userAgent.get())
    }

    @Test
    fun `follows redirects to the actual response`() = withServer { server, baseUri ->
        server.createContext("/redirect") { exchange ->
            exchange.use {
                it.responseHeaders.add("Location", "/content")
                it.sendResponseHeaders(302, -1)
            }
        }
        server.createContext("/content") { exchange ->
            exchange.use {
                val body = "downloaded content".toByteArray()
                it.sendResponseHeaders(200, body.size.toLong())
                it.responseBody.write(body)
            }
        }

        assertEquals("downloaded content", fetchHttpGetAsString(baseUri.resolve("/redirect")).body())
    }

    @Test
    fun `rejects HTTP error responses with status and URI`() = withServer { server, baseUri ->
        server.createContext("/missing") { exchange ->
            exchange.use { it.sendResponseHeaders(404, -1) }
        }
        val uri = baseUri.resolve("/missing")

        val exception = assertFailsWith<IllegalStateException> { fetchHttpGetAsByteArray(uri) }

        assertTrue(exception.message.orEmpty().contains("404"))
        assertTrue(exception.message.orEmpty().contains(uri.toString()))
    }

    private fun HttpServer.serveJar(body: ByteArray) {
        createContext("/server.jar") { exchange ->
            exchange.use {
                it.sendResponseHeaders(200, body.size.toLong())
                it.responseBody.write(body)
            }
        }
    }

    private fun sha256(bytes: ByteArray): String =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))

    // https://github.com/typst-io/spigradle/issues/122
    @Test
    fun `downloads into the file when the checksum matches`() = withServer { server, baseUri ->
        val body = "paper server".toByteArray()
        server.serveJar(body)
        val target = dir.resolve("server.jar").apply { writeText("cached server") }

        // the checksum comparison is case-insensitive
        fetchHttpGetToFile(baseUri.resolve("/server.jar"), target.toPath(), sha256(body).uppercase())

        assertContentEquals(body, target.readBytes())
    }

    // https://github.com/typst-io/spigradle/issues/122
    @Test
    fun `keeps the existing file when the checksum mismatches`() = withServer { server, baseUri ->
        server.serveJar("tampered server".toByteArray())
        val target = dir.resolve("server.jar").apply { writeText("cached server") }
        val expected = sha256("paper server".toByteArray())

        val exception = assertFailsWith<IllegalStateException> {
            fetchHttpGetToFile(baseUri.resolve("/server.jar"), target.toPath(), expected)
        }

        assertContains(exception.message.orEmpty(), expected)
        assertEquals("cached server", target.readText())
        assertEquals(emptyList(), dir.list()!!.filter { it.endsWith(".part") }, "temporary files are left")
    }

    // https://github.com/typst-io/spigradle/issues/123
    @Test
    fun `fails when the server doesn't respond within the timeout`() = withServer { server, baseUri ->
        val release = CountDownLatch(1)
        server.createContext("/hang") { exchange ->
            exchange.use {
                release.await(10, TimeUnit.SECONDS)
                it.sendResponseHeaders(204, -1)
            }
        }
        try {
            assertFailsWith<HttpTimeoutException> {
                fetchHttpGet(baseUri.resolve("/hang"), HttpResponse.BodyHandlers.ofString(), Duration.ofMillis(500))
            }
        } finally {
            release.countDown()
        }
    }

    // https://github.com/typst-io/spigradle/issues/123
    @Test
    fun `does not time out while receiving a body slower than the timeout`() = withServer { server, baseUri ->
        server.createContext("/slow") { exchange ->
            exchange.use {
                it.sendResponseHeaders(200, 4)
                repeat(4) { _ ->
                    it.responseBody.write('a'.code)
                    it.responseBody.flush()
                    Thread.sleep(400)
                }
            }
        }

        val response = fetchHttpGet(baseUri.resolve("/slow"), HttpResponse.BodyHandlers.ofString(), Duration.ofMillis(500))

        assertEquals("aaaa", response.body())
    }
}
