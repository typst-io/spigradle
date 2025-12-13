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

import io.typst.spigradle.common.BungeeDependencies
import io.typst.spigradle.common.SpigotDependencies
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by JunHyung Im on 2020-08-18
 */
internal fun File.createDirectories(): File = apply {
    parentFile.mkdirs()
}

internal fun File.writeGroovy(@Language("Groovy") contents: String): File = apply { writeText(contents) }

@ExperimentalStdlibApi
class GradleFunctionalTest {
    @TempDir
    lateinit var dir: File
    lateinit var buildFile: File
    lateinit var buildFileKt: File
    lateinit var settingsFile: File
    lateinit var settingsFileKt: File
    lateinit var subBuildFile: File
    lateinit var subSettingsFile: File
    lateinit var javaFile: File
    lateinit var javaFileB: File
    lateinit var javaFileC: File
    lateinit var kotlinFile: File
    lateinit var subJavaFile: File

    private fun createGradleRunner() = GradleRunner.create()
        .withProjectDir(dir)
        .withPluginClasspath()

    @BeforeTest
    fun setup() {
        buildFile = dir.resolve("build.gradle").createDirectories()
        buildFileKt = dir.resolve("build.gradle.kts")
        settingsFile = dir.resolve("settings.gradle").createDirectories().writeGroovy(
            """
            rootProject.name = 'main'
            include('sub')
        """.trimIndent()
        )
        settingsFileKt = dir.resolve("settings.gradle.kts")
        subBuildFile = dir.resolve("sub/build.gradle").createDirectories()
        subSettingsFile = dir.resolve("sub/settings.gradle").createDirectories()
        javaFile = dir.resolve("src/main/java/Main.java").createDirectories()
        javaFileB = dir.resolve("src/main/java/MainB.java").createDirectories()
        javaFileC = dir.resolve("src/main/java/MainC.java").createDirectories()
        kotlinFile = dir.resolve("src/main/kotlin/Main.kt").createDirectories()
        subJavaFile = dir.resolve("sub/src/main/java/Main.java").createDirectories()
    }

    @Test
    fun `main detection task incremental`() {
        buildFile.writeText(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle'
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()
                }
            }
            dependencies {
                compileOnly("${SpigotDependencies.SPIGOT_API.format()}")
            }
            java {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }
            
            detectSpigotMain {
                doLast {
                    assert outputFile.get().getAsFile().text == mainParam
                }
            }
        """.trimIndent()
        )

        javaFile.writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public class Main extends MainB {}
        """.trimIndent()
        )
        javaFileB.writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public abstract class MainB extends MainC {}
        """.trimIndent()
        )
        javaFileC.writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public abstract class MainC extends JavaPlugin {}
        """.trimIndent()
        )

        val result = createGradleRunner().withArguments("assemble", "-s", "-i", "-PmainParam=Main").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":detectSpigotMain")?.outcome)
        println(result.output)
    }

    @Test
    fun `main detection task update`() {
        buildFile.writeText(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle'
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()
                }
            }
            dependencies {
                compileOnly("${SpigotDependencies.SPIGOT_API.format()}")
            }
            java {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }
            detectSpigotMain {
                doLast {
                    assert outputFile.get().getAsFile().text == mainParam
                }
            }
        """.trimIndent()
        )

        // step 1: normal
        javaFile.writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public class Main extends JavaPlugin {}
        """.trimIndent()
        )
        assertDoesNotThrow {
            val result = createGradleRunner().withArguments("assemble", "-s", "-i", "-PmainParam=Main").build()
            assertEquals(TaskOutcome.SUCCESS, result.task(":detectSpigotMain")?.outcome)
            println(result.output)
        }

        // step 2: rename main class
        javaFile.delete()
        javaFileB.writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public class MainB extends JavaPlugin {}
        """.trimIndent()
        )
        val result = createGradleRunner().withArguments("assemble", "-s", "-i", "-PmainParam=MainB").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":detectSpigotMain")?.outcome)
        println(result.output)

        // step 3: up to date
        val resultB = createGradleRunner().withArguments("assemble", "-s", "-i", "-PmainParam=MainB").build()
        assertEquals(TaskOutcome.UP_TO_DATE, resultB.task(":detectSpigotMain")?.outcome)
        println(resultB.output)
    }

    @Test
    fun `jdk21 java`() {
        buildFile.writeText(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle'
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()
                }
            }
            dependencies {
                compileOnly("${SpigotDependencies.SPIGOT_API.format()}")
            }
            java {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }
        """.trimIndent()
        )
        javaFile.writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public class Main extends JavaPlugin {}
        """.trimIndent()
        )
        assertDoesNotThrow {
            val result = createGradleRunner().withArguments("assemble", "-s", "-i").build()
            assertEquals(TaskOutcome.SUCCESS, result.task(":assemble")?.outcome)
        }
    }

    @Test
    fun `jdk21 kotlin`() {
        buildFileKt.writeText(
            """
            import io.typst.spigradle.spigot.*
            plugins {
                kotlin("jvm") version "2.2.0"
                id("io.typst.spigradle")
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()                
                }
            }
            dependencies {
                compileOnly("${SpigotDependencies.SPIGOT_API.format()}")
                implementation(kotlin("stdlib"))
            }
            spigot {
                description.set("A test plugin")
            }
            kotlin {
                jvmToolchain(21)
            }
        """.trimIndent()
        )
        kotlinFile.writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin
            class Main : JavaPlugin()
        """.trimIndent()
        )
        assertDoesNotThrow {
            val result = createGradleRunner().withArguments("assemble", "-s").build()
            assertEquals(TaskOutcome.SUCCESS, result.task(":assemble")?.outcome)
        }
    }

    @Test
    @Ignore
    fun `apply scala and spigradle on a subproject`() {
        subBuildFile.writeGroovy(
            """ 
            plugins {
                id 'scala'
                id 'idea'
                id 'io.typst.spigradle'
            }
            spigot.main = 'Main'
            
            scala {
                scalaVersion = '2.13.12'
            }
        """.trimIndent()
        )
        assertDoesNotThrow {
            createGradleRunner().build()
        }
    }

    @Test
    fun `test description default value`() {
        buildFile.writeGroovy(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle'
            }
            description 'My awesome plugin'
            version '3.2.1'
            spigot.main = 'AwesomePlugin'
            generateSpigotDescription { doLast {
                [
                    "main": "AwesomePlugin",
                    "version": project.version,
                    "description": project.description
                ].each { k, v -> assert properties[k]?.getOrNull() == v }
            } }
        """.trimIndent()
        )
        val result = createGradleRunner().withArguments("generateSpigotDescription").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateSpigotDescription")?.outcome)
    }

    @Test
    fun `serialize bungee description`() {
        val bungeeDescFile = dir.resolve("build/tmp/generateBungeeDescription/bungee.yml")
        buildFile.writeGroovy(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle.bungee'
            }
            version = '1.0'
            bungee.main = 'MyPlugin'
        """.trimIndent()
        )
        val result = createGradleRunner().withArguments("generateBungeeDescription", "-s").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateBungeeDescription")?.outcome)
        assertEquals(
            """
            |main: MyPlugin
            |name: Main
            |version: '1.0'
        |""".trimMargin(), bungeeDescFile.readText()
        )
    }

    @Test
    fun `serialize nukkit description`() {
        val nukkitDescFile = dir.resolve("build/tmp/generateNukkitDescription/plugin.yml")
        buildFile.writeGroovy(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle.nukkit'
            }
            version = '1.0'
            nukkit.main = 'MyPlugin'
        """.trimIndent()
        )
        val result = createGradleRunner().withArguments("generateNukkitDescription", "-s").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateNukkitDescription")?.outcome)
        assertEquals(
            """
            |main: MyPlugin
            |name: Main
            |version: '1.0'
         |""".trimMargin(), nukkitDescFile.readText()
        )
    }

    @Test
    fun `apply both plugins spigot and bungee`() {
        buildFile.writeGroovy(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle'
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
                compileOnly("${SpigotDependencies.SPIGOT_API.format()}")
                compileOnly("${BungeeDependencies.BUNGEE_CORD.format()}")
            }
            
            generateSpigotDescription {
                doLast {
                    assert properties["main"].get() == "Main"
                }
            }
            generateBungeeDescription {
                doLast {
                    assert properties["main"].get() == "MainB"
                }
            }
        """.trimIndent()
        )
        javaFile.writeGroovy(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public class Main extends JavaPlugin {
            }
        """.trimIndent()
        )
        javaFileB.writeGroovy(
            """
            import net.md_5.bungee.api.plugin.Plugin;
            public class MainB extends Plugin {
            }
        """.trimIndent()
        )
        val result =
            createGradleRunner().withArguments("generateSpigotDescription", "generateBungeeDescription", "-i").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateSpigotDescription")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateBungeeDescription")?.outcome)
    }

    @Test
    fun `automatic main class detection groovy`() {
        buildFile.writeGroovy(
            """
            plugins {
                id 'java'
                id 'io.typst.spigradle'
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()
                }
            }
            dependencies {
                compileOnly("${SpigotDependencies.SPIGOT_API.format()}")                                                     
            }
        """.trimIndent()
        )
        javaFile.writeGroovy(
            """
            import org.bukkit.plugin.java.JavaPlugin;
            public class Main extends JavaPlugin {
            }
        """.trimIndent()
        )
        assertDoesNotThrow {
            val result = createGradleRunner().withArguments("assemble", "-i").build()
            assertEquals(TaskOutcome.SUCCESS, result.task(":assemble")?.outcome)
        }
    }

    @Test
    fun `automatic main class detection kotlin`() {
        buildFileKt.writeText(
            """
            import io.typst.spigradle.spigot.*
            plugins {
                kotlin("jvm") version "2.2.20"
                id("io.typst.spigradle")
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()
                }
            }
            dependencies {
                compileOnly("${SpigotDependencies.SPIGOT_API.format()}")
            }
        """.trimIndent()
        )
        kotlinFile.writeText(
            """
            import org.bukkit.plugin.java.JavaPlugin
            class Main : JavaPlugin()
        """.trimIndent()
        )
        assertDoesNotThrow {
            val result = createGradleRunner().withArguments("assemble", "-s").build()
            assertEquals(TaskOutcome.SUCCESS, result.task(":assemble")?.outcome)
        }
    }
}