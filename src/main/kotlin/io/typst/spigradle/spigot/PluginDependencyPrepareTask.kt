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

import io.typst.spigradle.ContentSource
import io.typst.spigradle.YamlValue
import io.typst.spigradle.fetchHttpGetAsByteArray
import io.typst.spigradle.fetchHttpGetAsString
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
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
 *    `compileClasspath` as a file-based dependency(file() or fileTree()), it will be copied directly.
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

    @get:Input
    val downloadSoftDepends: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

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
        val thePluginNames = pluginNames.get()
        val downloadSoftDepends = downloadSoftDepends.get()
        if (thePluginNames.isEmpty()) {
            logger.log(LogLevel.LIFECYCLE, "SKIPPED: no dependencies configured")
            return
        }

        // fetch all plugins from classpath
        val runtimeCp = project.configurations.named("compileClasspath")
        val pluginFiles = mutableMapOf<String, File>()

        val artifacts = runtimeCp.get().incoming
            .artifactView {
                isLenient = true
            }
            .artifacts
            .resolvedArtifacts
            .get()
        for (artifact in artifacts) {
            val file = artifact.file
            if (!file.name.endsWith(".jar")) {
                continue
            }
            val componentId = artifact.variant.owner
            val isFromRepository = componentId is ModuleComponentIdentifier
            val isFromProject = componentId is ProjectComponentIdentifier
            val isLocalFile = !isFromRepository && !isFromProject  // 로컬 파일 종속성
            if (!isLocalFile) {
                continue
            }
            val yamlText = readPluginYaml(file) ?: continue
            val yaml = YamlValue.parse(yamlText)
            val pluginName = yaml.get("name").value?.toString()
            if (pluginName != null) {
                pluginFiles[pluginName] = file
            }
        }

        // resolve the all plugin dependencies recursively
        val allPluginDependencies = mutableMapOf<String, SpigotPluginDependency>()
        val allPluginDepends = thePluginNames.toMutableList()

        var index = 0
        while (index < allPluginDepends.size) {
            val name = allPluginDepends[index]
            if (name !in allPluginDependencies) {
                val outputFile = outputDir.get().asFile.resolve("${name}.jar")
                val classpathFile = pluginFiles[name]
                val dependency =
                    parsePluginDependency(outputFile, local = false)
                        ?: parsePluginDependency(classpathFile, local = true)
                        ?: downloadPluginDependency(name)
                if (dependency != null) {
                    val depends = if (downloadSoftDepends) {
                        dependency.depends + dependency.softDepends
                    } else dependency.depends

                    allPluginDepends.addAll(depends)
                    allPluginDependencies[dependency.name] = dependency
                }
            }
            index++
        }

        val allPluginDependSet = allPluginDepends.toSet()
        logger.log(LogLevel.LIFECYCLE, "Preparing dependencies: ${allPluginDependSet.joinToString(", ", "[", "]")}")

        // skip if all exist
        if (allPluginDependSet.all {
                outputDir.get().asFile.resolve("${it}.jar").isFile
            }) {
            logger.log(LogLevel.LIFECYCLE, "SKIPPED: All files exists, skipping write $thePluginNames")
            return
        }

        val maxSize = allPluginDependSet.maxBy { it.length }.length
        for (name in allPluginDependSet) {
            val dependency = allPluginDependencies[name]
            if (dependency != null) {
                val outputFile = outputDir.get().asFile.resolve("${name}.jar")
                when (val source = dependency.contentSource) {
                    is ContentSource.Exist -> {
                        project.logger.log(
                            LogLevel.LIFECYCLE,
                            "SKIPPED ${name.padEnd(maxSize)} : file already exists, skipping write."
                        )
                    }

                    is ContentSource.LocalFile -> {
                        Files.copy(source.file.toPath(), outputFile.toPath())
                        project.logger.log(
                            LogLevel.LIFECYCLE,
                            "SUCCESS ${name.padEnd(maxSize)} : copied from classpath ${source.file.absolutePath}."
                        )
                    }

                    is ContentSource.Memory -> {
                        Files.write(outputFile.toPath(), source.bytes)
                        project.logger.log(
                            LogLevel.LIFECYCLE,
                            "SUCCESS ${name.padEnd(maxSize)} : downloaded from SpigotMC ${source.uri}."
                        )
                    }
                }
            } else {
                project.logger.log(
                    LogLevel.WARN,
                    "FAILED  ${name.padEnd(maxSize)} : not found on the classpath or in Spiget search results. you can either put the JAR manually or declare it as a file dependency in Gradle."
                )
            }
        }
    }

    internal fun downloadPluginDependency(name: String): SpigotPluginDependency? {
        val uri = URI("https://api.spiget.org/v2/search/resources/${name}?field=name&sort=-downloads")
        val results = YamlValue.parse(fetchHttpGetAsString(uri).body()).asList() ?: emptyList()

        for (resultYaml in results) {
            project.logger.log(
                LogLevel.DEBUG, "Spiget query result: $resultYaml"
            )
            val idValue = resultYaml.get("id").value?.toString() ?: continue
            val downloadUri = URI("https://api.spiget.org/v2/resources/$idValue/download")
            project.logger.log(
                LogLevel.LIFECYCLE, "Downloading $name from SpigotMC, uri=${downloadUri}"
            )
            val downloadFetchResult = fetchHttpGetAsByteArray(downloadUri)
            val bytes = downloadFetchResult.body()

            val yaml = readPluginYaml(ByteArrayInputStream(bytes))
            val dependency = if (yaml != null) {
                parsePluginDependency(yaml, ContentSource.Memory(bytes, downloadUri))
            } else null

            if (dependency?.name == name) {
                return dependency
            }
        }
        return null
    }

    internal fun parsePluginDependency(xs: String, source: ContentSource): SpigotPluginDependency? {
        val yaml = YamlValue.parse(xs)
        val name = yaml.get("name").value?.toString()
        val depends = yaml.get("depend").asList()?.mapNotNull { it.value?.toString() } ?: emptyList()
        val softDepends = yaml.get("softdepend").asList()?.mapNotNull { it.value?.toString() } ?: emptyList()
        return if (name != null) {
            SpigotPluginDependency(name, depends.toSet(), softDepends.toSet(), source)
        } else null
    }

    internal fun parsePluginDependency(
        input: InputStream,
        source: ContentSource,
        charset: Charset = StandardCharsets.UTF_8,
    ): SpigotPluginDependency? {
        return JarInputStream(input).use { jin ->
            generateSequence { jin.nextJarEntry }
                .firstOrNull { !it.isDirectory && it.name == "plugin.yml" }
                ?: return null

            val text = jin.bufferedReader(charset).readText()
            parsePluginDependency(text, source)
        }
    }

    // NOTE: file optional for convenience
    internal fun parsePluginDependency(
        file: File?,
        local: Boolean,
        charset: Charset = StandardCharsets.UTF_8,
    ): SpigotPluginDependency? {
        if (file == null || !file.isFile) {
            return null
        }
        val source = if (local) {
            ContentSource.LocalFile(file)
        } else ContentSource.Exist(file)
        return parsePluginDependency(FileInputStream(file), source, charset)
    }

    companion object {
        internal fun readPluginYaml(input: InputStream, charset: Charset = StandardCharsets.UTF_8): String? {
            return JarInputStream(input).use { jin ->
                generateSequence { jin.nextJarEntry }
                    .firstOrNull { !it.isDirectory && it.name == "plugin.yml" }
                    ?: return null

                jin.bufferedReader(charset).readText()
            }
        }

        internal fun readPluginYaml(jar: File, charset: Charset = StandardCharsets.UTF_8): String? {
            return readPluginYaml(FileInputStream(jar), charset)
        }
    }
}
