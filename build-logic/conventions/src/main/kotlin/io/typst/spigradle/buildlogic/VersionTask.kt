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

package io.typst.spigradle.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class VersionTask : DefaultTask() {
    companion object {
        fun getVersionFile(project: Project) = File("${project.projectDir}/version.txt")
        fun readVersion(project: Project) = getVersionFile(project).readText()
    }

    @get:Input
    @get:Option(option = "build-version", description = "Configure the version of Spigradle.")
    abstract val version: Property<String>

    @TaskAction
    fun execute() {
        getVersionFile(project).writeText(version.get())
        project.version = version.get()
    }
}