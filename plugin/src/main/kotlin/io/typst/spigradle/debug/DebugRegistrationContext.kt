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

package io.typst.spigradle.debug

import io.typst.spigradle.asCamelCase
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File

internal data class DebugRegistrationContext(
    val platformName: String,
    val platformVersion: Property<String>,
    val downloadURI: String,
    val debugArtifactRelativeDir: String,
    val jarFile: Provider<RegularFile>?,
    val jvmArgs: ListProperty<String>,
    val programArgs: ListProperty<String>,
    val jvmDebugPort: Property<Int>,
    val javaExecutable: Provider<RegularFile>,
    val overwrite: Boolean = false,
    val eula: Provider<Boolean>? = null,
    val downloadTask: TaskProvider<out Task>? = null,
    val extraTasks: Iterable<TaskProvider<out Task>> = emptyList(),
) {
    val taskGroupName: String get() = "$platformName debug"
    val downloadTaskName: String get() = "download${platformName.asCamelCase(true)}"

    internal fun getRunDebugTaskName(project: Project): String {
        return "debug${project.name.asCamelCase(true)}"
    }

    internal fun getDebugArtifactDir(project: Project): File {
        return File(getPlatformDebugDir(project).asFile, debugArtifactRelativeDir)
    }

    internal fun getPlatformDebugDir(project: Project): Directory {
        return project.layout.projectDirectory
            .dir(".gradle/spigradle-debug/${platformName}")
    }

    internal fun getDownloadBaseDir(project: Project): File {
        val userHome = project.gradle.gradleUserHomeDir
        return userHome.resolve("spigradle-debug-jars")
            .resolve(platformName)
    }

    // use global(user) dir
    internal fun getDownloadOutputDir(project: Project): Provider<File> {
        return platformVersion.map { ver ->
            getDownloadBaseDir(project)
                .resolve(ver)
        }
    }

    internal fun getDownloadOutputFile(project: Project): Provider<RegularFile> {
        return project.layout.file(getDownloadOutputDir(project).map {
            it.resolve("${platformName}.jar")
        })
    }
}
