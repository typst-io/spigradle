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
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HttpExtensionsTest {
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
}
