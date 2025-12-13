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

import org.gradle.api.Named
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

abstract class Permission : Named {
    abstract val description: Property<String>
    /**
     * Available values:
     * - true
     * - false
     * - op
     * - not op
     *
     * See: [https://www.spigotmc.org/wiki/plugin-yml/]
     */
    abstract val defaults: Property<String>
    abstract val children: MapProperty<String, Boolean>

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "description" to description.orNull,
            "default" to defaults.orNull,
            "children" to children.orNull?.ifEmpty { null }
        ).filterValues { it != null }
    }
}