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
 * Prepares plugin dependencies for the debug server environment.
 *
 * This task automatically resolves and provisions plugin dependencies needed by the Spigot plugin
 * being developed. It attempts to acquire each dependency plugin through two methods:
 *
 * 1. **From Gradle classpath**: If the dependency is already configured in the project's
 *    `compileClasspath` as a JAR artifact, it will be copied directly.
 * 2. **From SpigotMC**: If not found in the classpath, the task searches for the plugin on
 *    SpigotMC (via the Spiget API) and downloads the most popular matching resource.
 *
 * Each dependency is saved into the [outputDir] (typically `plugins/`) with the filename
 * `{name}.jar`, where `{name}` matches the plugin name declared in its `plugin.yml`.
 *
 * **Execution flow:**
 * 1. Skips execution if no plugin names are configured
 * 2. Skips execution if all plugin JARs already exist in [outputDir]
 * 3. Scans the project's `compileClasspath` to find JARs with matching plugin.yml names
 * 4. For each plugin in [pluginNames]:
 *    - Skips if the file already exists
 *    - Copies from classpath if found there
 *    - Otherwise, searches SpigotMC via Spiget API and downloads the most popular match
 *    - Validates the downloaded plugin.yml name matches the expected name
 *    - Logs SUCCESS, SKIPPED, or FAILED status for each plugin
 *
 * **Important behaviors:**
 * - This task does NOT overwrite existing files; you can manually place plugin JARs if needed
 * - Plugin names are sourced from the `depends` and `softDepends` configuration in your plugin.yml
 * - The task validates that downloaded plugins have the correct name in their plugin.yml
 * - Uses the `compileClasspath` configuration to search for plugin JARs
 */
open class PluginDependencyPrepareTask : DefaultTask() {
    init {
        group = "spigradle debug"
        description = "Prepare plugin dependencies"
    }

    /**
     * List of plugin names to prepare (e.g., `["Vault", "WorldEdit"]`).
     *
     * These names should match the plugin names defined in each dependency's `plugin.yml`.
     * Typically populated from the `depends` and `softDepends` configuration of the
     * project's Spigot plugin.
     */
    @get:Input
    val pluginNames: ListProperty<String> = project.objects.listProperty(String::class.java)

    /**
     * The output directory where plugin dependency JARs will be placed.
     *
     * This is typically the `plugins/` directory in the debug server folder
     * (e.g., `.gradle/spigradle-debug/spigot/plugins/`).
     */
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
