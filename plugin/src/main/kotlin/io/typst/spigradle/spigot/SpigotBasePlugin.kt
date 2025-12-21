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

import io.typst.spigradle.debug.DebugExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.create

/**
 * The Spigot base plugin that provides:
 *
 * Extensions (configuration blocks, NOT tasks):
 * - spigot([SpigotExtension]): extension for the Spigot environment
 * - repositories#spigotRepos([PaperRepositoryExtension]): extension for Spigot repository DSL.
 * - debugSpigot([DebugExtension]): extension for Spigot (Paper) debugging (configures `debug${ProjectName}` task)
 *     - jvmArgs: defaults to `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${jvmDebugPort}`
 *     - programArgs: defaults to `nogui`
 */
class SpigotBasePlugin : Plugin<Project> {
    companion object {
        @JvmStatic
        val PLATFORM_NAME: String = "spigot"
    }

    override fun apply(project: Project) {
        // register repo extension
        (project.repositories as ExtensionAware).extensions.create(
            "spigotRepos",
            PaperRepositoryExtension::class,
            project
        )
        // register spigot extension
        project.extensions.create(PLATFORM_NAME, SpigotExtension::class)
        // register paper debug extension
        project.extensions.create("debugSpigot", DebugExtension::class.java).apply {
            jvmArgs.convention(jvmDebugPort.map { port ->
                listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${port}")
            })
            programArgs.convention(listOf("nogui"))
        }
    }
}