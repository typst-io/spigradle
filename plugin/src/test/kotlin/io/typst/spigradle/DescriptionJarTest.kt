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

import io.typst.spigradle.catalog.BungeeDependencies
import io.typst.spigradle.catalog.PaperDependencies
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.jar.JarFile
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull

// https://github.com/typst-io/spigradle/issues/119
class DescriptionJarTest {
    @TempDir
    lateinit var dir: File

    @BeforeTest
    fun setup() {
        dir.resolve("settings.gradle").writeGroovy("rootProject.name = 'main'")
    }

    private fun writeJavaClass(name: String, superclass: String) {
        dir.resolve("src/main/java/$name.java").createDirectories().writeText(
            "public class $name extends $superclass {}"
        )
    }

    private fun buildJar() {
        GradleRunner.create()
            .withProjectDir(dir)
            .withPluginClasspath()
            .withArguments("jar", "--stacktrace")
            .build()
    }

    private fun readJarEntry(name: String): String =
        JarFile(dir.resolve("build/libs/main.jar")).use { jar ->
            val entry = assertNotNull(jar.getJarEntry(name), "The jar doesn't contain '$name'")
            jar.getInputStream(entry).bufferedReader().readText()
        }

    @Test
    fun `jar contains the generated plugin yml`() {
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
            """.trimIndent()
        )
        writeJavaClass("Main", "org.bukkit.plugin.java.JavaPlugin")

        buildJar()

        assertContains(readJarEntry("plugin.yml"), "main: Main")
    }

    @Test
    fun `jar contains descriptions of every applied platform`() {
        dir.resolve("build.gradle").writeGroovy(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle.spigot'
                id 'io.typst.spigradle.bungee'
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()
                }
                bungeeRepos {
                    bungeecord()
                    minecraftLibraries()
                }
            }
            dependencies {
                compileOnly("${PaperDependencies.SPIGOT_API.dependency.toGAV()}")
                compileOnly("${BungeeDependencies.BUNGEECORD_API.dependency.toGAV()}")
            }
            """.trimIndent()
        )
        writeJavaClass("Main", "org.bukkit.plugin.java.JavaPlugin")
        writeJavaClass("MainB", "net.md_5.bungee.api.plugin.Plugin")

        buildJar()

        assertContains(readJarEntry("plugin.yml"), "main: Main")
        assertContains(readJarEntry("bungee.yml"), "main: MainB")
    }
}
