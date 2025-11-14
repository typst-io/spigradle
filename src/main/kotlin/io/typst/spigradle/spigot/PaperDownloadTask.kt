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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.nio.file.Files

open class PaperDownloadTask : DefaultTask() {
    init {
        group = "spigradle debug"
        description = "Download a paper stable jar"
    }

    @Input
    val version: Property<String> = project.objects.property(String::class.java)

    @OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    @Suppress("UNCHECKED_CAST")
    @TaskAction
    fun download() {
        // get url
        val buildJsonURI = URI("https://fill.papermc.io/v3/projects/paper/versions/${version.get()}/builds")
        val getUrlResponse = fetchHttpGetAsString(buildJsonURI)
        val yamlStr = getUrlResponse.body()
        val yaml = YamlValue.parse(yamlStr).asList() ?: emptyList()
        val firstYaml = yaml.firstOrNull()?.asMap() ?: emptyMap()
        val url = firstYaml["downloads"]?.get("server:default")?.get("url")?.value?.toString()
            ?: throw IllegalStateException("Unknown json schema: $yamlStr")

        // read file
        val downloadResponse = fetchHttpGetAsByteArray(URI(url))
        Files.write(outputFile.get().asFile.toPath(), downloadResponse.body())
    }
}
