package io.typst.spigradle

import io.typst.spigradle.spigot.SpigotExtension
import io.typst.spigradle.spigot.SpigotPlugin
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateYamlTaskTest {
    val project = ProjectBuilder.builder().build()
    val yamlTask = project.tasks.register<YamlGenerate>("yaml").get()
    val file get() = File(yamlTask.temporaryDir, "plugin.yml")

    init {
        yamlTask.outputFiles.forEach {
            it.deleteOnExit()
        }
    }

    @Test
    fun `simple generation`() {
        val pairs = listOf("value" to "test contents")
        yamlTask.apply {
            properties.set(pairs.toMap().toMutableMap())
            outputFiles.from(file)
            generate()
        }
        val text = pairs.joinToString { (key, value) -> "$key: $value\n" }
        assertEquals(text, file.readText())
    }

    @Test
    fun `simple serialization`() {
        val extension = project.extensions.create<SpigotExtension>("spigot", project).apply {
            main.set("SpigradleMain")
        }
        yamlTask.apply {
            properties.set(
                project.getMainDetectivePropertiesProvider(
                    extension.toMap(),
                    project.getMainDetectOutputFile(SpigotPlugin.platformName)
                )
            )
            outputFiles.from(file)
            generate()
        }
        assertEquals(
            "main: SpigradleMain\n" +
                    "name: Test\n" +
                    "version: unspecified\n", file.readText()
        )
    }

    @Test
    fun `detail serialization`() {
        val ext = project.extensions.create<SpigotExtension>("spigot", project).apply {
            main.set("io.typst.spigradle.Main")
            name.set("Spigradle")
            version.set("1.1")
            description.set("This plugin does so much stuff it can't be contained!")
            website.set("https://github.com/spigradle/spigradle")
            authors.set(listOf("EntryPoint"))
            apiVersion.set("1.15")
            load.set("POSTWORLD")
            prefix.set("Its prefix")
            softDepend.set(listOf("ProtocolLib"))
            loadBefore.set(listOf("ABC"))
            libraries.set(
                listOf(
                    "com.squareup.okhttp3:okhttp:4.9.0",
                    "a:b:1.0.0"
                )
            )
            commands.apply {
                create("give").apply {
                    description.set("Give command.")
                    usage.set("/<command> [test|stop]")
                    permission.set("test.foo")
                    permissionMessage.set("You do not have permission!")
                    aliases.set(listOf("alias"))
                }
            }
            permissions.apply {
                create("test.*").apply {
                    description.set("Wildcard permission")
                    defaults.set("op")
                    children.set(mapOf("test.foo" to true))
                }
                create("test.foo").apply {
                    description.set("Allows foo command")
                    defaults.set("true")
                }
            }
        }
        yamlTask.apply {
            outputFiles.from(file)
            properties.set(
                project.getMainDetectivePropertiesProvider(
                    ext.toMap(),
                    project.getMainDetectOutputFile(SpigotPlugin.platformName)
                )
            )
            generate()
        }
        val expected =
            javaClass.getResourceAsStream("/spigot/plugin.yml").bufferedReader().readText().normaliseLineSeparators()
        assertEquals(expected, file.readText())
    }
}