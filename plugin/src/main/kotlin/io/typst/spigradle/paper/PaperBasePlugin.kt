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

package io.typst.spigradle.paper

import io.typst.spigradle.debug.DebugExtension
import io.typst.spigradle.spigot.PaperRepositoryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.create

class PaperBasePlugin : Plugin<Project> {
    companion object {
        @JvmStatic
        val PLATFORM_NAME: String = "paper"
    }

    override fun apply(project: Project) {
        // register repo extension
        (project.repositories as ExtensionAware).extensions.create(
            "paperRepos",
            PaperRepositoryExtension::class,
            project
        )
        // register paper extension
        project.extensions.create(PLATFORM_NAME, PaperExtension::class)
        // register paper debug extension
        project.extensions.create("debugPaper", DebugExtension::class.java).apply {
            jvmArgs.convention(jvmDebugPort.map { port ->
                listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${port}")
            })
            programArgs.convention(listOf("nogui"))
        }
    }
}