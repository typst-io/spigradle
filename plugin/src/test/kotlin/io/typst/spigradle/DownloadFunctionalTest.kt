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
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class DownloadFunctionalTest {
    @TempDir
    lateinit var dir: File

    @Test
    fun `preserves cached downloads and refreshes on every overwrite execution`() {
        val requests = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/server.jar") { exchange ->
            exchange.use {
                val body = "server-${requests.incrementAndGet()}".toByteArray()
                it.sendResponseHeaders(200, body.size.toLong())
                it.responseBody.write(body)
            }
        }
        server.start()
        try {
            dir.resolve("settings.gradle").writeText("rootProject.name = 'download-test'")
            dir.resolve("build.gradle").writeText(
                """
                plugins {
                    id 'io.typst.spigradle.spigot-base'
                }
                tasks.register('downloadFile', io.typst.spigradle.Download) {
                    uri.set(URI.create('http://127.0.0.1:${server.address.port}/server.jar'))
                    outputFile.set(layout.buildDirectory.file('downloads/server.jar'))
                    overwrite.set(providers.gradleProperty('overwrite').map { it.toBoolean() })
                }
                """.trimIndent()
            )
            fun run(overwrite: Boolean) = GradleRunner.create()
                .withProjectDir(dir)
                .withPluginClasspath()
                .withArguments("downloadFile", "-Poverwrite=$overwrite", "--stacktrace")
                .build()

            val outputFile = dir.resolve("build/downloads/server.jar")
            assertEquals(TaskOutcome.SUCCESS, run(false).task(":downloadFile")?.outcome)
            assertEquals("server-1", outputFile.readText())

            assertEquals(TaskOutcome.SKIPPED, run(false).task(":downloadFile")?.outcome)
            assertEquals("server-1", outputFile.readText())
            assertEquals(1, requests.get())

            assertEquals(TaskOutcome.SUCCESS, run(true).task(":downloadFile")?.outcome)
            assertEquals("server-2", outputFile.readText())

            assertEquals(TaskOutcome.SUCCESS, run(true).task(":downloadFile")?.outcome)
            assertEquals("server-3", outputFile.readText())
            assertEquals(3, requests.get())
        } finally {
            server.stop(0)
        }
    }
}
