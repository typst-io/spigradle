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

import io.typst.spigradle.bungee.BungeePlugin
import io.typst.spigradle.catalog.BungeeDependencies
import io.typst.spigradle.catalog.PaperDependencies
import io.typst.spigradle.nukkit.NukkitPlugin
import io.typst.spigradle.spigot.SpigotPlugin
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.BeforeTest
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
        val superclass = SpigotPlugin.spec.propertyBySuperclass.entries.first().key
        buildFile.writeText(
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
                compileOnly("${PaperDependencies.SPIGOT_API.format()}")
            }
            java {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }
            
            ${SpigotPlugin.DETECT_ENTRYPOINTS_TASK_NAME} {
                doLast {
                    assert outputFileBySuperclass.get()["$superclass"].getAsFile().text == mainParam
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
        assertEquals(TaskOutcome.SUCCESS, result.task(":${SpigotPlugin.DETECT_ENTRYPOINTS_TASK_NAME}")?.outcome)
        println(result.output)
    }

    @Test
    fun `main detection task update`() {
        val superclass = SpigotPlugin.spec.propertyBySuperclass.entries.first().key
        buildFile.writeText(
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
                compileOnly("${PaperDependencies.SPIGOT_API.format()}")
            }
            java {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }
            ${SpigotPlugin.DETECT_ENTRYPOINTS_TASK_NAME} {
                doLast {
                    assert outputFileBySuperclass.get()["$superclass"].getAsFile().text == mainParam
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
            assertEquals(TaskOutcome.SUCCESS, result.task(":${SpigotPlugin.DETECT_ENTRYPOINTS_TASK_NAME}")?.outcome)
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
        assertEquals(TaskOutcome.SUCCESS, result.task(":${SpigotPlugin.DETECT_ENTRYPOINTS_TASK_NAME}")?.outcome)
        println(result.output)

        // step 3: up to date
        val resultB = createGradleRunner().withArguments("assemble", "-s", "-i", "-PmainParam=MainB").build()
        assertEquals(TaskOutcome.UP_TO_DATE, resultB.task(":${SpigotPlugin.DETECT_ENTRYPOINTS_TASK_NAME}")?.outcome)
        println(resultB.output)
    }

    @Test
    fun `jdk21 java`() {
        buildFile.writeText(
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
                compileOnly("${PaperDependencies.SPIGOT_API.format()}")
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
            plugins {
                kotlin("jvm") version "2.2.0"
                id("io.typst.spigradle.spigot")
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()                
                }
            }
            dependencies {
                compileOnly("${PaperDependencies.SPIGOT_API.format()}")
                implementation(kotlin("stdlib"))
            }
            spigot {
                description.set("A test plugin")
            }
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(21)
                }
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
    fun `apply scala and spigradle on a subproject`() {
        subBuildFile.writeGroovy(
            """ 
            plugins {
                id 'scala'
                id 'io.typst.spigradle.spigot'
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
                id 'io.typst.spigradle.spigot'
            }
            description 'My awesome plugin'
            version '3.2.1'
            spigot.main = 'AwesomePlugin'
            ${SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME} { doLast {
                [
                    "main": "AwesomePlugin",
                    "version": project.version,
                    "description": project.description
                ].each { k, v -> assert properties[k]?.getOrNull() == v }
            } }
        """.trimIndent()
        )
        val result = createGradleRunner().withArguments(SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME).build()
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":${SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME}")?.outcome
        )
    }

    @Test
    fun `serialize bungee description`() {
        val bungeeDescFile = dir.resolve("build/tmp/${BungeePlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME}/bungee.yml")
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
        val result =
            createGradleRunner().withArguments(BungeePlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME, "-s").build()
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":${BungeePlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME}")?.outcome
        )
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
        val nukkitDescFile = dir.resolve("build/tmp/${NukkitPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME}/plugin.yml")
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
        val result =
            createGradleRunner().withArguments(NukkitPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME, "-s").build()
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":${NukkitPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME}")?.outcome
        )
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
                compileOnly("${PaperDependencies.SPIGOT_API.format()}")
                compileOnly("${BungeeDependencies.BUNGEE_CORD.format()}")
            }
            
            ${SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME} {
                doLast {
                    assert properties.get()["main"] == "Main"
                }
            }
            ${BungeePlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME} {
                doLast {
                    assert properties.get()["main"] == "MainB"
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
            createGradleRunner().withArguments(
                SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME,
                BungeePlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME,
                "-i"
            ).build()
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":${SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME}")?.outcome
        )
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":${BungeePlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME}")?.outcome
        )
    }

    @Test
    fun `automatic main class detection groovy`() {
        buildFile.writeGroovy(
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
                compileOnly("${PaperDependencies.SPIGOT_API.format()}")                                                     
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
            plugins {
                kotlin("jvm") version "2.2.20"
                id("io.typst.spigradle.spigot")
            }
            repositories {
                mavenCentral()
                spigotRepos {
                    spigotmc()
                }
            }
            dependencies {
                compileOnly("${PaperDependencies.SPIGOT_API.format()}")
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