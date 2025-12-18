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

package io.typst.spigradle.nukkit

import io.typst.spigradle.ModuleRegistrationContext
import io.typst.spigradle.asCamelCase
import io.typst.spigradle.getMainDetectOutputFile
import io.typst.spigradle.registerDescGenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

/**
 * The Nukkit plugin that provides:
 *
 * Applies plugins:
 * - java-base([JavaBasePlugin])
 * - io.typst.spigradle.nukkit-base([NukkitBasePlugin])
 *
 * Tasks:
 * - generateNukkitDescription([io.typst.spigradle.YamlGenerate]) task for the 'plugin.yml' generation.
 * - detectNukkitMain([io.typst.spigradle.SubclassDetection]) task for the main-class detection.
 */
class NukkitPlugin : Plugin<Project> {
    companion object {
        val platformName = "nukkit"
        val genDescTask: String = "generate${platformName.asCamelCase(true)}Description"
        val mainDetectTask: String = "detect${platformName.asCamelCase(true)}Main"

        fun createModuleRegistrationContext(
            project: Project,
            extension: NukkitExtension,
        ): ModuleRegistrationContext<NukkitExtension> {
            return ModuleRegistrationContext(
                platformName,
                "plugin.yml",
                extension,
                project.getMainDetectOutputFile(platformName),
                genDescTask,
                mainDetectTask,
                "cn/nukkit/plugin/PluginBase"
            )
        }
    }

    override fun apply(project: Project) {
        project.pluginManager.apply(JavaBasePlugin::class.java)
        project.pluginManager.apply(NukkitBasePlugin::class.java)

        val extension = project.extensions.getByType(NukkitExtension::class.java)
        val ctx = createModuleRegistrationContext(project, extension)
        registerDescGenTask(project, ctx) { desc ->
            desc.toMap()
        }
    }
}