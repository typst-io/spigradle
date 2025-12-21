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

import io.typst.spigradle.PlatformPluginSpec
import io.typst.spigradle.PluginDescriptionProperty
import io.typst.spigradle.nukkit.NukkitBasePlugin.Companion.PLATFORM_NAME
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
 * - generateNukkitPluginDescription([io.typst.spigradle.YamlGenerate]) task for the 'plugin.yml' generation.
 * - detectNukkitEntrypoints([io.typst.spigradle.SubclassDetection]) task for the main-class detection.
 */
class NukkitPlugin : Plugin<Project> {
    companion object {
        internal val spec: PlatformPluginSpec = PlatformPluginSpec(
            PLATFORM_NAME,
            "plugin.yml",
            listOf(
                PluginDescriptionProperty("main", "cn/nukkit/plugin/PluginBase", mandatory = true)
            ),
        )

        @JvmStatic
        val GENERATE_PLUGIN_DESCRIPTION_TASK_NAME: String = spec.generateDescriptionTaskName

        @JvmStatic
        val DETECT_ENTRYPOINTS_TASK_NAME: String = spec.detectEntrypointsTaskName
    }

    override fun apply(project: Project) {
        project.pluginManager.apply(JavaBasePlugin::class.java)
        project.pluginManager.apply(NukkitBasePlugin::class.java)

        val extension = project.extensions.getByType(NukkitExtension::class.java)
        val ctx = spec.createModuleRegistrationContext(project, project.provider { extension.toMap() })
        registerDescGenTask(project, ctx)
    }
}