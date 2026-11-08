/*
 * Copyright (c) 2025 Spigradle contributors.
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

import io.typst.spigradle.catalog.PaperDependencies
import io.typst.spigradle.spigot.SpigotPlugin
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

// https://github.com/typst-io/spigradle/issues/117
class ConfigurationCacheTest {
    @TempDir
    lateinit var dir: File

    @BeforeTest
    fun setup() {
        dir.resolve("settings.gradle").writeGroovy("rootProject.name = 'main'")
        dir.resolve("build.gradle").writeGroovy(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle.spigot'
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()
                }
            }
            dependencies {
                compileOnly("${PaperDependencies.SPIGOT_API.dependency.toGAV()}")
            }
            java {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }
            debugSpigot {
                version.set('1.21.8')
                eula.set(true)
            }
            """.trimIndent()
        )
    }

    private fun writeMainClass(name: String) {
        dir.resolve("src/main/java").deleteRecursively()
        dir.resolve("src/main/java/$name.java").createDirectories().writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public class $name extends JavaPlugin {}
            """.trimIndent()
        )
    }

    private fun build(vararg tasks: String): BuildResult = GradleRunner.create()
        .withProjectDir(dir)
        .withPluginClasspath()
        .withArguments(*tasks, "--configuration-cache", "--stacktrace")
        .build()
        .also { println(it.output) }

    private fun pluginYaml(): String =
        dir.resolve("build/resources/main/plugin.yml").readText()

    @Test
    fun `plugin description generation stores and reuses the configuration cache`() {
        writeMainClass("Main")

        val first = build("assemble")
        assertEquals(
            TaskOutcome.SUCCESS,
            first.task(":${SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME}")?.outcome
        )
        assertContains(first.output, "Configuration cache entry stored.")
        assertContains(pluginYaml(), "main: Main")

        val second = build("assemble")
        assertContains(second.output, "Reusing configuration cache.")
        assertContains(pluginYaml(), "main: Main")
    }

    @Test
    fun `detected main class is not stale when the configuration cache is reused`() {
        writeMainClass("Main")
        build("assemble")

        // Only sources change, so the configuration cache entry is reused.
        writeMainClass("RenamedMain")
        val result = build("assemble")

        assertContains(result.output, "Reusing configuration cache.")
        assertContains(pluginYaml(), "main: RenamedMain")
    }

    @Test
    fun `preparing plugin dependencies is configuration cache compatible`() {
        writeMainClass("Main")

        val first = build("preparePluginDependencies")
        assertContains(first.output, "Configuration cache entry stored.")

        val second = build("preparePluginDependencies")
        assertContains(second.output, "Reusing configuration cache.")
    }

    private fun debugFiles(): List<String> =
        dir.resolve(".gradle/spigradle-debug").walkTopDown().filter { it.isFile }.map { it.name }.toList()

    @Test
    fun `debug preparation tasks are configuration cache compatible`() {
        writeMainClass("Main")

        val first = build("copyArtifactJar", "createJavaDebugScript")
        assertContains(first.output, "Configuration cache entry stored.")
        assertEquals(TaskOutcome.SUCCESS, first.task(":copyArtifactJar")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, first.task(":createJavaDebugScript")?.outcome)
        val files = debugFiles()
        assertContains(files, "main.jar")
        assertContains(files, "eula.txt")
        assertContains(files, "starter")
        assertContains(files, "starter.bat")

        val second = build("copyArtifactJar", "createJavaDebugScript")
        assertContains(second.output, "Reusing configuration cache.")
    }

    @Test
    fun `debug task graph can be stored in the configuration cache`() {
        writeMainClass("Main")

        // dry-run: store the whole debug task graph without downloading or launching the server
        val first = build("debugMain", "--dry-run")
        assertContains(first.output, "Configuration cache entry stored.")

        val second = build("debugMain", "--dry-run")
        assertContains(second.output, "Reusing configuration cache.")
    }
}
