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

package io.typst.spigradle.catalog

data class Dependency(
    val group: String,
    val artifact: String,
    val version: String,
    val label: String,
    val isLocal: Boolean = false,
    val tags: List<String> = emptyList(),
    val versionRef: String = label,
) {
    fun getTaggedVersion(inputVersion: String?): String {
        if (inputVersion == null) return version
        if (tags.isEmpty()) return inputVersion
        return buildString {
            val pieces = inputVersion.split("-")
            append(inputVersion)
            for (tag in tags.drop(pieces.size - 1)) {
                append("-").append(tag)
            }
        }
    }

    fun toGA(): String =
        "${group}:${artifact}"

    fun toGAV(inputVersion: String? = null): String {
        return "${toGA()}:${getTaggedVersion(inputVersion)}"
    }

    companion object {
        val SNAPSHOT_TAG: List<String> = listOf("SNAPSHOT")
        val SPIGOT_VERSION_TAGS: List<String> = listOf("R0.1", "SNAPSHOT")
    }
}