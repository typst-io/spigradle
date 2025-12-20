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

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

internal data class ModuleRegistrationContext(
    val platformName: String,
    val descFileName: String,
    val descriptionProperties: Provider<Map<String, Any?>>,
    val pluginDescriptionProperties: Provider<List<PluginDescriptionProperty>>,
    val detectEntrypointsTaskName: String,
    val generateDescriptionTaskName: String,
) {
    fun getOutputFileBySuperclass(): Provider<Map<String, RegularFile>> {
        return pluginDescriptionProperties.map {
            it.mapNotNull { prop ->
                val file = prop.valueFallbackFile
                if (file != null) {
                    prop.superclass to file
                } else null
            }.toMap()
        }
    }

    fun getFileFallbackProperties(): Provider<Map<String, RegularFile>> {
        return pluginDescriptionProperties.map { properties ->
            properties
                .mapNotNull {
                    if (it.valueFallbackFile != null) {
                        it.name to it.valueFallbackFile
                    } else null
                }
                .associate { (property, file) ->
                    property to file
                }
        }
    }

    fun getDetectionOutputFiles(): Provider<List<RegularFile>> {
        return pluginDescriptionProperties.map { properties ->
            properties.mapNotNull { it.valueFallbackFile }
        }
    }
}
