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

import javax.inject.Inject

// @JsonPropertyOrder("description", "default", "children")
open class Permission @Inject constructor(@Transient val name: String) {
    var description: String? = null

    // @SerialName("default")
    var defaults: String? = null
    var children = emptyMap<String, Boolean>()

    fun serialize(): Map<String, Any?> {
        return mapOf(
            "description" to description,
            "default" to defaults,
            "children" to children.ifEmpty { null }
        ).filterValues { it != null }
    }
}