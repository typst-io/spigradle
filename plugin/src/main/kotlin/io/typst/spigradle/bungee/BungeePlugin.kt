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

package io.typst.spigradle.bungee

import io.typst.spigradle.ModuleRegistrationContext
import io.typst.spigradle.asCamelCase
import io.typst.spigradle.bungee.BungeeBasePlugin.Companion.PLATFORM_NAME
import io.typst.spigradle.getMainDetectOutputFile
import io.typst.spigradle.registerDescGenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

/**
 * The BungeeCord plugin that provides:
 *
 * Applies plugins:
 * - java-base([JavaBasePlugin])
 * - io.typst.spigradle.bungee-base([BungeeBasePlugin])
 *
 * Tasks:
 * - generateBungeeDescription([io.typst.spigradle.YamlGenerate]) task for the 'bungee.yml' generation.
 * - detectBungeeMain([io.typst.spigradle.SubclassDetection]) task for the main-class detection.
 */
class BungeePlugin : Plugin<Project> {
    companion object {
        @JvmStatic
        val GENERATE_DESCRIPTION_TASK_NAME: String = "generate${PLATFORM_NAME.asCamelCase(true)}Description"
        @JvmStatic
        val DETECT_MAIN_TASK_NAME: String = "detect${PLATFORM_NAME.asCamelCase(true)}Main"

        fun createModuleRegistrationContext(
            project: Project,
            extension: BungeeExtension,
        ): ModuleRegistrationContext<BungeeExtension> {
            return ModuleRegistrationContext(
                PLATFORM_NAME,
                "bungee.yml",
                extension,
                project.getMainDetectOutputFile(PLATFORM_NAME),
                GENERATE_DESCRIPTION_TASK_NAME,
                DETECT_MAIN_TASK_NAME,
                "net/md_5/bungee/api/plugin/Plugin"
            )
        }
    }

    override fun apply(project: Project) {
        project.pluginManager.apply(JavaBasePlugin::class.java)
        project.pluginManager.apply(BungeeBasePlugin::class.java)

        val extension = project.extensions.getByType(BungeeExtension::class.java)
        val ctx = createModuleRegistrationContext(project, extension)
        registerDescGenTask(project, ctx) { desc ->
            desc.toMap()
        }
    }
}
