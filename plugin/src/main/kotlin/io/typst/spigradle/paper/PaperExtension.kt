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

import io.typst.spigradle.asCamelCase
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class PaperExtension @Inject constructor(private val project: Project) {
    abstract val main: Property<String>
    abstract val name: Property<String>
    abstract val version: Property<String>
    abstract val description: Property<String>
    abstract val apiVersion: Property<String>
    abstract val bootstrapper: Property<String>
    abstract val loader: Property<String>
    abstract val bootstrapDependencies: NamedDomainObjectContainer<PaperPluginDependency>
    abstract val serverDependencies: NamedDomainObjectContainer<PaperPluginDependency>

    /**
     * Whether detect the bootstrapper class, a Spigradle unique property
     */
    abstract val enableBootstrapper: Property<Boolean>
    /**
     * Whether detect the loader class, a Spigradle unique property
     */
    abstract val enableLoader: Property<Boolean>

    init {
        name.convention(project.provider { project.name.asCamelCase(true) })
        version.convention(project.provider { project.version.toString() })
        description.convention(project.provider { project.description })
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "main" to main.orNull,
            "name" to name.orNull,
            "version" to version.orNull,
            "description" to description.orNull,
            "api-version" to apiVersion.orNull,
            "bootstrapper" to bootstrapper.orNull,
            "loader" to loader.orNull,
            "dependencies" to mapOf(
                "bootstrap" to bootstrapDependencies.toList().associate {
                    it.name to it.toMap()
                }.ifEmpty { null },
                "server" to serverDependencies.toList().associate {
                    it.name to it.toMap()
                }.ifEmpty { null },
            ).filterValues { it != null }.ifEmpty { null }
        )
    }
}
