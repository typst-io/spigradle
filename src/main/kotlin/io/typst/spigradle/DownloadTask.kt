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
import java.nio.file.Files

open class DownloadTask : DefaultTask() {
    init {
        group = "spigradle"
        description = "Download a file into the given path from the given uri"
        onlyIf { !overwrite.get() || !outputFile.get().asFile.isFile }
    }

    @Input
    val uri: Property<URI> = project.objects.property(URI::class.java)

    @Input
    val overwrite: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    @OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun download() {
        val target = outputFile.get().asFile.toPath()
        val bytes = fetchHttpGetAsByteArray(uri.get())
        Files.write(target, bytes.body())
    }
}
