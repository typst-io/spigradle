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

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URI

/**
 * Downloads a file from a given URI and saves it to a specified location.
 *
 * This is a general-purpose download task that fetches content via HTTP GET
 * and writes it to the local filesystem. It supports optional overwrite control
 * and will skip execution if the output file already exists (unless overwrite is enabled).
 *
 * The task is configured to run only when:
 * - [overwrite] is `true`, OR
 * - The [outputFile] does not exist
 *
 * Groovy Example:
 * ```groovy
 * import io.typst.spigradle.Download
 *
 * task downloadFile(type: Download) {
 *   uri = URI.create('https://example.com/file.jar')
 *   outputFile = file('build/downloads/file.jar')
 *   overwrite = false
 * }
 * ```
 *
 * Kotlin Example:
 * ```kotlin
 * import io.typst.spigradle.Download
 * import java.net.URI
 *
 * tasks {
 *   val downloadFile by registering(Download::class) {
 *     uri.set(URI.create("https://example.com/file.jar"))
 *     outputFile.set(file("build/downloads/file.jar"))
 *     overwrite.set(false)
 *   }
 * }
 * ```
 *
 * @since 1.3.0
 */
abstract class Download : DefaultTask() {
    init {
        group = "spigradle"
        description = "Download a file into the given path from the given uri"
        overwrite.convention(false)
        onlyIf { overwrite.get() || !outputFile.get().asFile.isFile }
        outputs.upToDateWhen { !overwrite.get() }
    }

    /**
     * The URI to download from.
     *
     * This should be a valid HTTP or HTTPS URL pointing to the file to download.
     */
    @get:Input
    abstract val uri: Property<URI>

    /**
     * Whether to overwrite an existing file.
     *
     * - When `true`: Downloads the file even if it already exists at [outputFile]
     * - When `false` (default): Skips the download if [outputFile] already exists
     *
     * Defaults to `false`.
     */
    @get:Input
    abstract val overwrite: Property<Boolean>

    /**
     * The output file location where the downloaded content will be saved.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun download() {
        fetchHttpGetToFile(uri.get(), outputFile.get().asFile.toPath())
    }
}
