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

// @JsonPropertyOrder("description", "usage", "permission", "permission-message")
open class Command @Inject constructor(@Transient val name: String) {
    var description: String? = null
    var usage: String? = null
    var permission: String? = null

    // @SerialName("permission-message")
    var permissionMessage: String? = null
    var aliases = emptyList<String>()

    fun aliases(vararg aliases: String) {
        this.aliases = aliases.toList()
    }

    fun serialize(): Map<String, Any?> {
        return mapOf(
            "description" to description,
            "usage" to usage,
            "permission" to permission,
            "permission-message" to permissionMessage,
            "aliases" to aliases.ifEmpty { null }
        ).filterValues { it != null }
    }
}
