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

/**
 * Downloads a Paper (or Spigot) server JAR from PaperMC's download API.
 *
 * This task automates the process of downloading a specific version of the Paper server
 * for local development and debugging. It performs the following steps:
 *
 * 1. Queries the PaperMC API at `https://fill.papermc.io/v3/projects/paper/versions/{version}/builds`
 *    to get the list of available builds for the specified version
 * 2. Selects the first (latest stable) build from the response
 * 3. Extracts the download URL for the server JAR
 * 4. Downloads the JAR file and writes it to the configured output location
 *
 * The downloaded JAR is cached in the global Spigradle cache directory
 * (`$GRADLE_USER_HOME/spigradle-debug-jars/`) and used by the debug system to run
 * a local server for plugin testing.
 *
 * @throws IllegalStateException if the API response doesn't match the expected schema
 * @see io.typst.spigradle.debug.DebugTask
 */
open class PaperDownloadTask : DefaultTask() {
    init {
        group = "spigradle debug"
        description = "Download a paper stable jar"
    }

    /**
     * The Minecraft version to download (e.g., "1.21.4", "1.20.6").
     *
     * This version string is used to query the PaperMC API for available builds.
     */
    @get:Input
    val version: Property<String> = project.objects.property(String::class.java)

    /**
     * The output file location where the downloaded Paper JAR will be saved.
     *
     * Typically points to the global Spigradle cache directory at
     * `$GRADLE_USER_HOME/spigradle-debug-jars/`.
     */
    @get:OutputFile
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
