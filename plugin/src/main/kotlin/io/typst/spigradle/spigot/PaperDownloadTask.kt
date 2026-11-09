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
import io.typst.spigradle.fetchHttpGetAsString
import io.typst.spigradle.fetchHttpGetToFile
import io.typst.spigradle.sha256Of
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

/**
 * Downloads a Paper (or Spigot) server JAR from PaperMC's download API.
 *
 * This task automates the process of downloading a specific version of the Paper server
 * for local development and debugging. It performs the following steps:
 *
 * 1. Queries the PaperMC API at `https://fill.papermc.io/v3/projects/paper/versions/{version}/builds`
 *    to get the list of available builds for the specified version
 * 2. Selects the latest build from the response, regardless of its channel (ALPHA, BETA, STABLE)
 * 3. Skips the download if the cached JAR already matches the checksum of that build
 * 4. Otherwise downloads the JAR file and writes it to the configured output location
 *
 * The task always runs, because a newer build can be published while the version stays the same.
 * When offline, or when the API can't be reached, an already cached JAR is used as is.
 *
 * The downloaded JAR is cached in the global Spigradle cache directory
 * (`$GRADLE_USER_HOME/spigradle-debug-jars/`) and used by the debug system to run
 * a local server for plugin testing.
 *
 * @throws IllegalStateException if the API response doesn't match the expected schema
 * @see io.typst.spigradle.debug.DebugTask
 */
abstract class PaperDownloadTask : DefaultTask() {
    init {
        group = "spigradle debug"
        description = "Download the latest paper jar"
        offline.convention(false)
        outputs.upToDateWhen { false }
    }

    /**
     * The Minecraft version to download (e.g., "1.21.4", "1.20.6").
     *
     * This version string is used to query the PaperMC API for available builds.
     */
    @get:Input
    abstract val version: Property<String>

    /**
     * The output file location where the downloaded Paper JAR will be saved.
     *
     * Typically points to the global Spigradle cache directory at
     * `$GRADLE_USER_HOME/spigradle-debug-jars/`.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Whether Gradle runs with `--offline`; if so, the cached JAR is used without querying the API.
     */
    @get:Internal
    abstract val offline: Property<Boolean>

    @TaskAction
    fun download() {
        val version = version.get()
        val target = outputFile.get().asFile.toPath()
        val cached = Files.isRegularFile(target)
        if (offline.get()) {
            if (!cached) {
                throw GradleException("Paper $version isn't downloaded yet, run without --offline")
            }
            didWork = false
            return
        }
        val build = try {
            val buildJsonURI = URI("https://fill.papermc.io/v3/projects/paper/versions/$version/builds")
            findLatestBuild(fetchHttpGetAsString(buildJsonURI).body(), version)
        } catch (e: Exception) {
            if (!cached) throw e
            logger.warn("Couldn't check the latest build of Paper $version, using the cached jar: ${e.message}")
            didWork = false
            return
        }
        if (isDownloaded(target, build)) {
            logger.info("Paper $version build #${build.id} is up to date")
            didWork = false
            return
        }
        logger.lifecycle("Downloading Paper $version build #${build.id}")
        fetchHttpGetToFile(build.url, target, build.sha256)
    }

    companion object {
        internal fun isDownloaded(target: Path, build: PaperBuild): Boolean =
            Files.isRegularFile(target) && sha256Of(target).equals(build.sha256, ignoreCase = true)

        internal fun findLatestBuild(buildsJson: String, version: String): PaperBuild {
            val builds = YamlValue.parse(buildsJson).asList()?.mapNotNull { it.asMap() }
                ?: throw IllegalStateException("Unknown json shcema: $buildsJson")
            val latest = builds.maxByOrNull {
                (it["id"]?.value as? Number)?.toInt() ?: -1
            } ?: throw IllegalStateException("No build of Paper $version")
            val download = latest["downloads"]?.get("server:default")
            val id = (latest["id"]?.value as? Number)?.toInt()
            val url = download?.get("url")?.value?.toString()
            val sha256 = download?.get("checksums")?.get("sha256")?.value?.toString()
            if (id == null || url == null || sha256 == null) {
                throw IllegalStateException("Unknown json schema: $buildsJson")
            }
            return PaperBuild(id, URI(url), sha256)
        }
    }
}

internal data class PaperBuild(val id: Int, val url: URI, val sha256: String)
