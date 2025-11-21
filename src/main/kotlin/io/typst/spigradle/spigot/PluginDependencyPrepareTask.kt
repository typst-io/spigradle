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

package io.typst.spigradle.spigot

import io.typst.spigradle.YamlValue
import io.typst.spigradle.fetchHttpGetAsByteArray
import io.typst.spigradle.fetchHttpGetAsString
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.jar.JarInputStream

/**
 * Prepare plugin dependencies:
 *
 * 1. Copy it from the classpath configured in Gradle(e.g. JAR file dependency),
 * 2. Or download it by searching for the most popular resource on SpigotMC
 *
 * Into `outputDir` (e.g. plugins), as `name.jar` (the name defined in plugin.yml).
 *
 * This task does not overwrite existing files; you can also place the file manually.
 */
open class PluginDependencyPrepareTask : DefaultTask() {
    init {
        group = "spigradle debug"
        description = "Prepare plugin dependencies"
    }

    @get:Input
    val pluginNames: ListProperty<String> = project.objects.listProperty(String::class.java)

    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    @TaskAction
    fun prepare() {
        if (pluginNames.get().isEmpty()) {
            logger.log(LogLevel.LIFECYCLE, "SKIPPED: no dependencies configured")
            return
        }

        // skip if all exist
        if (pluginNames.get().all {
                outputDir.get().asFile.resolve("${it}.jar").isFile
            }) {
            logger.log(LogLevel.LIFECYCLE, "SKIPPED: All files exists, skipping write ${pluginNames.get()}")
            return
        }

        val runtimeCp = project.configurations.named("compileClasspath")
        val pluginFiles = mutableMapOf<String, File>()
        val set = runtimeCp.get().resolve()
        for (file in set) {
            if (!file.name.endsWith(".jar")) {
                continue
            }
            val yaml = readPluginYaml(file) ?: continue
            val pluginName = parsePluginName(yaml)
            if (pluginName != null) {
                pluginFiles[pluginName] = file
            }
        }

        // TODO: match minecraft version
        // TODO: cache?
        // TODO: overwrite when getting from classpath?
        val maxSize = pluginNames.get().maxBy { it.length }.length
        for (name in pluginNames.get()) {
            val file = outputDir.get().asFile.resolve("${name}.jar")
            if (file.isFile) {
                project.logger.log(LogLevel.LIFECYCLE, "SKIPPED ${name.padEnd(maxSize)} : file already exists, skipping write.")
                continue
            }

            // method 1: from classpath
            val theFile = pluginFiles[name]
            if (theFile != null) {
                project.logger.log(LogLevel.LIFECYCLE, "SUCCESS ${name.padEnd(maxSize)} : copied from classpath ${theFile.absolutePath}")
                Files.copy(theFile.toPath(), file.toPath())
                continue
            }

            // method 2: spiget
            val uri = URI("https://api.spiget.org/v2/search/resources/${name}?field=name&sort=-downloads")
            val yaml = YamlValue.parse(fetchHttpGetAsString(uri).body()).asList()
            val idValue = yaml?.firstOrNull()?.get("id")?.value?.toString()
            if (idValue != null) {
                val downloadUri = URI("https://api.spiget.org/v2/resources/$idValue/download")
                val bytes = fetchHttpGetAsByteArray(downloadUri).body()

                val yaml = readPluginYaml(ByteArrayInputStream(bytes))
                val pluginName = parsePluginName(yaml ?: "")

                if (pluginName == name) {
                    Files.write(file.toPath(), bytes)
                    project.logger.log(LogLevel.LIFECYCLE, "SUCCESS ${name.padEnd(maxSize)} : downloaded from SpigotMC $downloadUri")
                } else {
                    project.logger.log(
                        LogLevel.WARN,
                        "FAILED  ${name.padEnd(maxSize)} : downloaded from SpigotMC but plugin.yml name mismatch '${name}', received '${pluginName}' (maybe the wrong name is configured in `depends/softDepends`), from $downloadUri"
                    )
                }
            } else {
                project.logger.log(
                    LogLevel.WARN,
                    "FAILED  ${name.padEnd(maxSize)} : not found on classpath nor in Spiget search results $uri"
                )
            }
        }
    }

    companion object {
        fun readPluginYaml(input: InputStream, charset: Charset = StandardCharsets.UTF_8): String? {
            return JarInputStream(input).use { jin ->
                generateSequence { jin.nextJarEntry }
                    .firstOrNull { !it.isDirectory && it.name == "plugin.yml" }
                    ?: return null

                jin.bufferedReader(charset).readText()
            }
        }

        fun readPluginYaml(jar: File, charset: Charset = StandardCharsets.UTF_8): String? {
            return readPluginYaml(FileInputStream(jar), charset)
        }

        fun parsePluginName(xs: String): String? {
            return YamlValue.parse(xs).get("name").value?.toString()
        }
    }
}
