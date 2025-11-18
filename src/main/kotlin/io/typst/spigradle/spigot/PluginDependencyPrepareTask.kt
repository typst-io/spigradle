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
import java.io.File
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.jar.JarFile

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
            return
        }

        // skip if all exist
        if (pluginNames.get().all {
            outputDir.get().asFile.resolve("${name}.jar").isFile
        }) {
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
        for (name in pluginNames.get()) {
            val file = outputDir.get().asFile.resolve("${name}.jar")
            if (file.isFile) {
                continue
            }

            // method 1: from classpath
            val theFile = pluginFiles[name]
            if (theFile != null) {
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
                Files.write(file.toPath(), bytes)
            } else {
                project.logger.log(LogLevel.WARN, "Couldn't find the plugin: $name")
            }
        }
    }

    companion object {
        fun readPluginYaml(jar: File, charset: Charset = StandardCharsets.UTF_8): String? {
            return JarFile(jar).use { jf ->
                val e = jf.getJarEntry("plugin.yml") ?: return null
                jf.getInputStream(e).bufferedReader(charset).use { it.readText() }
            }
        }

        fun parsePluginName(xs: String): String? {
            return YamlValue.parse(xs).get("name").value?.toString()
        }
    }
}
