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

import io.typst.spigradle.paper.PaperBasePlugin.Companion.PLATFORM_NAME
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal data class PlatformPluginSpec(
    val platformName: String,
    val pluginDescriptionFileName: String,
    val pluginDescriptionProperties: List<PluginDescriptionProperty>,
) {
    val generateDescriptionTaskName: String get() = "generate${platformName.asCamelCase(true)}PluginDescription"
    val detectEntrypointsTaskName: String get() = "detect${platformName.asCamelCase(true)}Entrypoints"

    internal fun createModuleRegistrationContext(
        project: Project,
        descriptionProvider: Provider<Map<String, Any?>>,
        lazyMandatory: Map<String, Provider<Boolean>> = emptyMap(),
    ): ModuleRegistrationContext {
        val pluginDescriptionProperties = pluginDescriptionProperties.map {
            project.provider {
                val mandatory = lazyMandatory[it.name]?.orNull ?: it.mandatory
                if (it.isFileFallbackProperty && mandatory) {
                    project.getSubclassDetectionFallbackProperty(platformName, it).get()
                } else it
            }
        }
        return ModuleRegistrationContext(
            PLATFORM_NAME,
            pluginDescriptionFileName,
            descriptionProvider,
            pluginDescriptionProperties.sequence(project),
            detectEntrypointsTaskName,
            generateDescriptionTaskName,
        )
    }
}
